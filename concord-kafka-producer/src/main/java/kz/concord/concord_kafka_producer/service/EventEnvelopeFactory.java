package kz.concord.concord_kafka_producer.service;

import kz.concord.concord_kafka_producer.event.DomainEvent;
import kz.concord.concord_kafka_producer.event.EventEnvelope;
import kz.concord.concord_kafka_producer.event.EventMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EventEnvelopeFactory {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${spring.application.version:unknown}")
    private String applicationVersion;

    public <T> EventEnvelope<T> createEnvelope(T payload) {
        return EventEnvelope.<T>builder()
            .eventType(payload.getClass().getSimpleName())
            .eventVersion("1.0")
            .timestamp(Instant.now())
            .source(applicationName)
            .contentType("application/json")
            .metadata(createDefaultMetadata())
            .payload(payload)
            .build();
    }

    public EventEnvelope<DomainEvent> createDomainEventEnvelope(DomainEvent domainEvent) {
        return EventEnvelope.<DomainEvent>builder()
            .eventType(domainEvent.getEventType())
            .eventVersion(domainEvent.getEventVersion())
            .timestamp(domainEvent.getOccurredOn())
            .source(domainEvent.getSource())
            .subject(domainEvent.getAggregateId())
            .contentType("application/json")
            .metadata(createDefaultMetadata())
            .payload(domainEvent)
            .build();
    }

    public <T> EventEnvelope<T> createEnvelopeWithMetadata(T payload, EventMetadata eventMetadata) {
        Map<String, Object> metadata = createDefaultMetadata();
        if (eventMetadata != null) {
            if (eventMetadata.getTenantId() != null) metadata.put("tenantId", eventMetadata.getTenantId());
            if (eventMetadata.getUserId() != null) metadata.put("userId", eventMetadata.getUserId());
            if (eventMetadata.getSessionId() != null) metadata.put("sessionId", eventMetadata.getSessionId());
            if (eventMetadata.getTraceId() != null) metadata.put("traceId", eventMetadata.getTraceId());
            if (eventMetadata.getSpanId() != null) metadata.put("spanId", eventMetadata.getSpanId());
            if (eventMetadata.getCustomAttributes() != null) {
                metadata.putAll(eventMetadata.getCustomAttributes());
            }
        }

        return EventEnvelope.<T>builder()
            .eventType(payload.getClass().getSimpleName())
            .eventVersion("1.0")
            .timestamp(Instant.now())
            .source(applicationName)
            .contentType("application/json")
            .metadata(metadata)
            .payload(payload)
            .build();
    }

    private Map<String, Object> createDefaultMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("applicationName", applicationName);
        metadata.put("applicationVersion", applicationVersion);
        metadata.put("producer", "concord-kafka-producer");
        return metadata;
    }
}