package kz.concord.concord_kafka_producer.event;

import java.time.Instant;

public interface DomainEvent {
    
    String getEventType();
    
    String getEventVersion();
    
    Instant getOccurredOn();
    
    String getAggregateId();
    
    default String getSource() {
        return this.getClass().getSimpleName();
    }
}