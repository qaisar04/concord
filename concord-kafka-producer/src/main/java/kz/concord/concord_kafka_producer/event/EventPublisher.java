package kz.concord.concord_kafka_producer.event;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {
    
    <T> CompletableFuture<Void> publish(String topic, T event);
    
    <T> CompletableFuture<Void> publish(String topic, String key, T event);
    
    <T> CompletableFuture<Void> publishEnvelope(String topic, EventEnvelope<T> envelope);
    
    <T> CompletableFuture<Void> publishEnvelope(String topic, String key, EventEnvelope<T> envelope);
    
    <T> CompletableFuture<Void> publishDomainEvent(String topic, DomainEvent event);
    
    <T> CompletableFuture<Void> publishDomainEvent(String topic, String key, DomainEvent event);
}