package kz.concord.concord_kafka_producer.config;

import io.micrometer.core.instrument.MeterRegistry;
import kz.concord.concord_kafka_producer.metrics.KafkaProducerMetrics;
import kz.concord.concord_kafka_producer.props.ConcordKafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "concord.kafka.producer.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaProducerMetricsConfiguration {

    private final ConcordKafkaProperties properties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public KafkaProducerMetrics kafkaProducerMetrics(MeterRegistry meterRegistry) {
        log.info("Configuring Kafka producer metrics with prefix: {}", 
            properties.getMetrics().getPrefix());
        return new KafkaProducerMetrics(meterRegistry);
    }
}