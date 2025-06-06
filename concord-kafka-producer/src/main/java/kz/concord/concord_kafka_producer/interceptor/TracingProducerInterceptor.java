package kz.concord.concord_kafka_producer.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class TracingProducerInterceptor implements ProducerInterceptor<String, Object> {

    private static final String TRACE_ID_HEADER = "trace-id";
    private static final String SPAN_ID_HEADER = "span-id";
    private static final String TIMESTAMP_HEADER = "timestamp";

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        Headers headers = record.headers();
        
        // Add trace ID if not present
        if (getHeader(headers, TRACE_ID_HEADER) == null) {
            headers.add(TRACE_ID_HEADER, UUID.randomUUID().toString().getBytes());
        }
        
        // Add span ID if not present
        if (getHeader(headers, SPAN_ID_HEADER) == null) {
            headers.add(SPAN_ID_HEADER, UUID.randomUUID().toString().getBytes());
        }
        
        // Add timestamp
        headers.add(TIMESTAMP_HEADER, String.valueOf(System.currentTimeMillis()).getBytes());
        
        log.debug("Added tracing headers to record for topic: {}", record.topic());
        
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            log.debug("Message acknowledged for topic: {}, partition: {}, offset: {}", 
                metadata.topic(), metadata.partition(), metadata.offset());
        } else {
            log.warn("Message failed for topic: {}", metadata != null ? metadata.topic() : "unknown", exception);
        }
    }

    @Override
    public void close() {
        log.debug("TracingProducerInterceptor closed");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.debug("TracingProducerInterceptor configured");
    }

    private Header getHeader(Headers headers, String key) {
        return headers.lastHeader(key);
    }
}