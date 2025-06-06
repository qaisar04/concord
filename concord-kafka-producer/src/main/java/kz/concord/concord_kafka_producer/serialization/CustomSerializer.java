package kz.concord.concord_kafka_producer.serialization;

import org.apache.kafka.common.serialization.Serializer;

public interface CustomSerializer<T> extends Serializer<T> {
    
    String getContentType();
    
    Class<T> getTargetClass();
    
    default boolean supportsType(Class<?> type) {
        return getTargetClass().isAssignableFrom(type);
    }
}