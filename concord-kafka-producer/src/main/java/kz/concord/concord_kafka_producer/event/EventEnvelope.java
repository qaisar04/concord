package kz.concord.concord_kafka_producer.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventEnvelope<T> {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String eventType;

    private String eventVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private Instant timestamp = Instant.now();

    private String source;

    private String subject;

    private String correlationId;

    private String causationId;

    private Map<String, Object> metadata;

    private T payload;

    private String contentType;

    @Builder.Default
    private String specVersion = "1.0";
}