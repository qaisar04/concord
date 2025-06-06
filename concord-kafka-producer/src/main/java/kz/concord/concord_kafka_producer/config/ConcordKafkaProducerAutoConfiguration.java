package kz.concord.concord_kafka_producer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.concord.concord_kafka_producer.batch.BatchEventPublisher;
import kz.concord.concord_kafka_producer.event.EventPublisher;
import kz.concord.concord_kafka_producer.interceptor.MetricsProducerInterceptor;
import kz.concord.concord_kafka_producer.interceptor.TracingProducerInterceptor;
import kz.concord.concord_kafka_producer.metrics.KafkaProducerMetrics;
import kz.concord.concord_kafka_producer.props.ConcordKafkaProperties;
import kz.concord.concord_kafka_producer.serialization.SerializerRegistry;
import kz.concord.concord_kafka_producer.service.ConcordEventPublisher;
import kz.concord.concord_kafka_producer.service.EventEnvelopeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass({KafkaTemplate.class})
@ConditionalOnProperty(prefix = "concord.kafka.producer", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ConcordKafkaProperties.class)
@RequiredArgsConstructor
@Import({KafkaProducerMetricsConfiguration.class, KafkaProducerTracingConfiguration.class})
public class ConcordKafkaProducerAutoConfiguration {

    private final ConcordKafkaProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<String, Object> concordKafkaProducerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = createProducerConfig();
        
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configProps);
        
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(objectMapper);
        jsonSerializer.setAddTypeInfo(properties.getSerialization().isAddTypeHeaders());
        factory.setValueSerializer(jsonSerializer);
        
        log.info("Configured Kafka producer factory with bootstrap servers: {}", 
            properties.getBootstrapServers());
            
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, Object> concordKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        
        template.setObservationEnabled(properties.getTracing().isEnabled());
        
        log.info("Configured Kafka template with tracing enabled: {}", properties.getTracing().isEnabled());
        
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventEnvelopeFactory eventEnvelopeFactory() {
        return new EventEnvelopeFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry(List.of());
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchEventPublisher batchEventPublisher(EventPublisher eventPublisher, KafkaProducerMetrics metrics) {
        return new BatchEventPublisher(eventPublisher, metrics);
    }



    @Bean
    @ConditionalOnMissingBean
    public EventPublisher concordEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaProducerMetrics metrics,
            EventEnvelopeFactory envelopeFactory) {
        return new ConcordEventPublisher(kafkaTemplate, metrics, envelopeFactory);
    }

    private Map<String, Object> createProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        configProps.put(ProducerConfig.RETRIES_CONFIG, properties.getRetry().getAttempts());
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, properties.getRetry().getBackoff().toMillis());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, properties.getRetry().isEnableIdempotence());
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 
            properties.getRetry().getMaxInFlightRequestsPerConnection());
        
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        if (properties.getSecurity() != null && !"PLAINTEXT".equals(properties.getSecurity().getProtocol())) {
            configureSecurity(configProps);
        }
        
        if (properties.getPartitioning().getPartitionerClass() != null) {
            configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, properties.getPartitioning().getPartitionerClass());
        }
        
        // Add producer interceptors
        List<String> interceptors = new ArrayList<>();
        if (properties.getTracing().isEnabled()) {
            interceptors.add(TracingProducerInterceptor.class.getName());
        }
        if (properties.getMetrics().isEnabled()) {
            interceptors.add(MetricsProducerInterceptor.class.getName());
        }
        if (!interceptors.isEmpty()) {
            configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);
        }
        
        configProps.putAll(properties.getAdditionalProperties());
        
        return configProps;
    }

    private void configureSecurity(Map<String, Object> configProps) {
        ConcordKafkaProperties.Security security = properties.getSecurity();
        
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, security.getProtocol());
        
        if (security.getUsername() != null && security.getPassword() != null) {
            configProps.put(SaslConfigs.SASL_MECHANISM, security.getMechanism());
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                security.getUsername(), security.getPassword()));
        }
        
        if (security.getTruststore() != null) {
            configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, security.getTruststore());
            if (security.getTruststorePassword() != null) {
                configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, security.getTruststorePassword());
            }
        }
        
        if (security.getKeystore() != null) {
            configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, security.getKeystore());
            if (security.getKeystorePassword() != null) {
                configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, security.getKeystorePassword());
            }
        }
    }
}