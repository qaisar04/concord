package kz.concord.concord_kafka_producer.interceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricsProducerInterceptor implements ProducerInterceptor<String, Object> {

    @Setter
    private MeterRegistry meterRegistry;
    private final Map<String, Long> sendTimes = new ConcurrentHashMap<>();

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        if (meterRegistry != null) {
            // Record send attempt
            Counter.builder("kafka.producer.send.attempts")
                .description("Number of send attempts")
                .tag("topic", record.topic())
                .register(meterRegistry)
                .increment();

            // Store send time for duration calculation
            String key = record.topic() + ":" + record.partition() + ":" + System.nanoTime();
            sendTimes.put(key, System.nanoTime());
        }
        
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (meterRegistry != null) {
            String status = exception == null ? "success" : "failure";
            String topic = metadata != null ? metadata.topic() : "unknown";
            
            // Record result
            Counter.builder("kafka.producer.send.results")
                .description("Number of send results")
                .tag("topic", topic)
                .tag("status", status)
                .register(meterRegistry)
                .increment();

            // Record timing if available
            if (metadata != null) {
                String keyPrefix = metadata.topic() + ":" + metadata.partition();
                sendTimes.entrySet().removeIf(entry -> {
                    if (entry.getKey().startsWith(keyPrefix)) {
                        long duration = System.nanoTime() - entry.getValue();
                        Timer.builder("kafka.producer.send.duration")
                            .description("Send operation duration")
                            .tag("topic", topic)
                            .tag("status", status)
                            .register(meterRegistry)
                            .record(duration, TimeUnit.NANOSECONDS);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    @Override
    public void close() {
        sendTimes.clear();
        log.debug("MetricsProducerInterceptor closed");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.debug("MetricsProducerInterceptor configured");
    }
}