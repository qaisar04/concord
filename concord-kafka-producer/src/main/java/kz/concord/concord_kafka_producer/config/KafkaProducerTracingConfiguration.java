package kz.concord.concord_kafka_producer.config;

import kz.concord.concord_kafka_producer.props.ConcordKafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
@ConditionalOnProperty(prefix = "concord.kafka.producer.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerTracingConfiguration {

    private final ConcordKafkaProperties properties;

    public KafkaProducerTracingConfiguration(ConcordKafkaProperties properties) {
        this.properties = properties;
        log.info("Kafka producer tracing configuration enabled with service name: {}", 
            properties.getTracing().getServiceName());
    }
}