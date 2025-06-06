package kz.concord.concord_kafka_producer.service;

import kz.concord.concord_kafka_producer.event.DomainEvent;
import kz.concord.concord_kafka_producer.event.EventEnvelope;
import kz.concord.concord_kafka_producer.event.EventPublisher;
import kz.concord.concord_kafka_producer.metrics.KafkaProducerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcordEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProducerMetrics metrics;
    private final EventEnvelopeFactory envelopeFactory;

    @Override
    public <T> CompletableFuture<Void> publish(String topic, T event) {
        return publish(topic, null, event);
    }

    @Override
    public <T> CompletableFuture<Void> publish(String topic, String key, T event) {
        EventEnvelope<T> envelope = envelopeFactory.createEnvelope(event);
        return publishEnvelope(topic, key, envelope);
    }

    @Override
    public <T> CompletableFuture<Void> publishEnvelope(String topic, EventEnvelope<T> envelope) {
        return publishEnvelope(topic, null, envelope);
    }

    @Override
    public <T> CompletableFuture<Void> publishEnvelope(String topic, String key, EventEnvelope<T> envelope) {
        return doPublish(topic, key, envelope, "envelope");
    }

    @Override
    public <T> CompletableFuture<Void> publishDomainEvent(String topic, DomainEvent event) {
        return publishDomainEvent(topic, event.getAggregateId(), event);
    }

    @Override
    public <T> CompletableFuture<Void> publishDomainEvent(String topic, String key, DomainEvent event) {
        EventEnvelope<DomainEvent> envelope = envelopeFactory.createDomainEventEnvelope(event);
        return doPublish(topic, key, envelope, "domain-event");
    }

    private <T> CompletableFuture<Void> doPublish(String topic, String key, T payload, String eventCategory) {
        long startTime = System.currentTimeMillis();
        
        log.debug("Publishing {} to topic: {}, key: {}", eventCategory, topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = key != null
            ? kafkaTemplate.send(topic, key, payload)
            : kafkaTemplate.send(topic, payload);

        return future
            .thenApply(result -> {
                long duration = System.currentTimeMillis() - startTime;
                metrics.recordSuccessfulPublish(topic, eventCategory, duration);
                
                if (result.getRecordMetadata() != null) {
                    log.debug("Successfully published {} to topic: {}, partition: {}, offset: {}", 
                        eventCategory, topic, 
                        result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
                } else {
                    log.debug("Successfully published {} to topic: {}", eventCategory, topic);
                }
                    
                return (Void) null;
            })
            .exceptionally(throwable -> {
                long duration = System.currentTimeMillis() - startTime;
                
                // Unwrap CompletionException if present
                Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                metrics.recordFailedPublish(topic, eventCategory, duration, cause);
                
                log.error("Failed to publish {} to topic: {}, key: {}", eventCategory, topic, key, cause);
                throw new RuntimeException("Failed to publish event", cause);
            });
    }
}