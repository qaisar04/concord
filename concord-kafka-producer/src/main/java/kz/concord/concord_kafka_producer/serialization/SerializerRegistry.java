package kz.concord.concord_kafka_producer.serialization;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SerializerRegistry {

    private final Map<Class<?>, CustomSerializer<?>> customSerializers = new ConcurrentHashMap<>();
    private final Map<String, CustomSerializer<?>> contentTypeSerializers = new ConcurrentHashMap<>();

    public SerializerRegistry(List<CustomSerializer<?>> customSerializers) {
        customSerializers.forEach(this::registerSerializer);
    }

    public void registerSerializer(CustomSerializer<?> serializer) {
        Class<?> targetClass = serializer.getTargetClass();
        String contentType = serializer.getContentType();
        
        customSerializers.put(targetClass, serializer);
        contentTypeSerializers.put(contentType, serializer);
        
        log.info("Registered custom serializer for type: {} with content type: {}", 
            targetClass.getSimpleName(), contentType);
    }

    @SuppressWarnings("unchecked")
    public <T> CustomSerializer<T> getSerializer(Class<T> type) {
        return (CustomSerializer<T>) customSerializers.entrySet().stream()
            .filter(entry -> entry.getValue().supportsType(type))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T> CustomSerializer<T> getSerializerByContentType(String contentType) {
        return (CustomSerializer<T>) contentTypeSerializers.get(contentType);
    }

    public boolean hasSerializer(Class<?> type) {
        return customSerializers.entrySet().stream()
            .anyMatch(entry -> entry.getValue().supportsType(type));
    }
}