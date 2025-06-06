package kz.concord.concord_kafka_producer.batch;

import kz.concord.concord_kafka_producer.event.EventPublisher;
import kz.concord.concord_kafka_producer.metrics.KafkaProducerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchEventPublisher {

    private final EventPublisher eventPublisher;
    private final KafkaProducerMetrics metrics;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, BatchBuffer> topicBuffers = new ConcurrentHashMap<>();

    public CompletableFuture<Void> publishBatch(String topic, List<?> events) {
        return publishBatch(topic, events, null);
    }

    public CompletableFuture<Void> publishBatch(String topic, List<?> events, String partitionKey) {
        log.debug("Publishing batch of {} events to topic: {}", events.size(), topic);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (Object event : events) {
            CompletableFuture<Void> future = partitionKey != null 
                ? eventPublisher.publish(topic, partitionKey, event)
                : eventPublisher.publish(topic, event);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                metrics.recordBatchSize(topic, events.size());
                log.debug("Successfully published batch of {} events to topic: {}", events.size(), topic);
            });
    }

    public void addToBatch(String topic, Object event, BatchConfig config) {
        BatchBuffer buffer = topicBuffers.computeIfAbsent(topic, k -> new BatchBuffer(topic, config));
        buffer.add(event);
    }

    public void flushBatch(String topic) {
        BatchBuffer buffer = topicBuffers.get(topic);
        if (buffer != null) {
            buffer.flush();
        }
    }

    public void flushAllBatches() {
        topicBuffers.values().forEach(BatchBuffer::flush);
    }

    private class BatchBuffer {
        private final String topic;
        private final BatchConfig config;
        private final List<Object> events = new ArrayList<>();
        private final Object lock = new Object();
        private volatile ScheduledFuture<?> flushTask;

        public BatchBuffer(String topic, BatchConfig config) {
            this.topic = topic;
            this.config = config;
        }

        public void add(Object event) {
            synchronized (lock) {
                events.add(event);
                
                // Schedule flush if this is the first event
                if (events.size() == 1) {
                    scheduleFlush();
                }
                
                // Flush immediately if batch size reached
                if (events.size() >= config.getMaxBatchSize()) {
                    flush();
                }
            }
        }

        public void flush() {
            List<Object> toFlush;
            synchronized (lock) {
                if (events.isEmpty()) {
                    return;
                }
                toFlush = new ArrayList<>(events);
                events.clear();
                cancelFlushTask();
            }

            publishBatch(topic, toFlush)
                .exceptionally(throwable -> {
                    log.error("Failed to flush batch for topic: {}", topic, throwable);
                    return null;
                });
        }

        private void scheduleFlush() {
            cancelFlushTask();
            flushTask = scheduler.schedule(this::flush, config.getFlushInterval().toMillis(), TimeUnit.MILLISECONDS);
        }

        private void cancelFlushTask() {
            if (flushTask != null && !flushTask.isDone()) {
                flushTask.cancel(false);
            }
        }
    }

    public static class BatchConfig {
        private final int maxBatchSize;
        private final Duration flushInterval;

        public BatchConfig(int maxBatchSize, Duration flushInterval) {
            this.maxBatchSize = maxBatchSize;
            this.flushInterval = flushInterval;
        }

        public static BatchConfig of(int maxBatchSize, Duration flushInterval) {
            return new BatchConfig(maxBatchSize, flushInterval);
        }

        public int getMaxBatchSize() { return maxBatchSize; }
        public Duration getFlushInterval() { return flushInterval; }
    }

    public void shutdown() {
        flushAllBatches();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}