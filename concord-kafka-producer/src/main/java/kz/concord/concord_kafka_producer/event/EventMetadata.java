package kz.concord.concord_kafka_producer.event;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EventMetadata {
    
    private String tenantId;
    private String userId;
    private String sessionId;
    private String traceId;
    private String spanId;
    private String applicationName;
    private String applicationVersion;
    private Map<String, Object> customAttributes;
    
    public static EventMetadata empty() {
        return EventMetadata.builder().build();
    }
    
    public EventMetadata withCustomAttribute(String key, Object value) {
        if (customAttributes == null) {
            customAttributes = Map.of(key, value);
        } else {
            customAttributes.put(key, value);
        }
        return this;
    }
}