package kz.concord.concord_kafka_producer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerMetrics {

    private final MeterRegistry meterRegistry;

    public void recordSuccessfulPublish(String topic, String eventCategory, long durationMs) {
        Counter.builder("kafka.producer.messages.sent")
            .description("Number of successfully sent messages")
            .tag("topic", topic)
            .tag("event_category", eventCategory)
            .tag("status", "success")
            .register(meterRegistry)
            .increment();

        Timer.builder("kafka.producer.send.duration")
            .description("Time taken to send messages")
            .tag("topic", topic)
            .tag("event_category", eventCategory)
            .tag("status", "success")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);

        log.debug("Recorded successful publish metric for topic: {}, category: {}, duration: {}ms", 
            topic, eventCategory, durationMs);
    }

    public void recordFailedPublish(String topic, String eventCategory, long durationMs, Throwable error) {
        Counter.builder("kafka.producer.messages.sent")
            .description("Number of failed messages")
            .tag("topic", topic)
            .tag("event_category", eventCategory)
            .tag("status", "failed")
            .tag("error_type", error.getClass().getSimpleName())
            .register(meterRegistry)
            .increment();

        Timer.builder("kafka.producer.send.duration")
            .description("Time taken to send messages")
            .tag("topic", topic)
            .tag("event_category", eventCategory)
            .tag("status", "failed")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);

        log.warn("Recorded failed publish metric for topic: {}, category: {}, duration: {}ms, error: {}", 
            topic, eventCategory, durationMs, error.getMessage());
    }

    public void recordRetry(String topic, int attemptNumber) {
        Counter.builder("kafka.producer.retries")
            .description("Number of retry attempts")
            .tag("topic", topic)
            .tag("attempt", String.valueOf(attemptNumber))
            .register(meterRegistry)
            .increment();
    }

    public void recordBatchSize(String topic, int batchSize) {
        meterRegistry.gauge("kafka.producer.batch.size", 
            io.micrometer.core.instrument.Tags.of("topic", topic), 
            batchSize);
    }
}