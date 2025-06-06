package kz.concord.concord_kafka_producer.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "concord.kafka.producer")
public class ConcordKafkaProperties {

    private boolean enabled = true;

    @NotBlank
    private String bootstrapServers = "localhost:9092";

    @Valid
    private Security security = new Security();

    @Valid
    private Serialization serialization = new Serialization();

    @Valid
    private Retry retry = new Retry();

    @Valid
    private Partitioning partitioning = new Partitioning();

    @Valid
    private Metrics metrics = new Metrics();

    @Valid
    private Tracing tracing = new Tracing();


    private Map<String, Object> additionalProperties = new HashMap<>();

    @Data
    public static class Security {
        private String protocol = "PLAINTEXT";
        private String mechanism = "PLAIN";
        private String username;
        private String password;
        private String truststore;
        private String truststorePassword;
        private String keystore;
        private String keystorePassword;
    }

    @Data
    public static class Serialization {
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
        private boolean addTypeHeaders = true;
        private Map<String, Object> properties = new HashMap<>();
    }

    @Data
    public static class Retry {
        @Positive
        private int attempts = 3;
        
        @NotNull
        private Duration backoff = Duration.ofMillis(1000);
        
        @NotNull
        private Duration maxBackoff = Duration.ofMillis(10000);
        
        private double backoffMultiplier = 2.0;
        
        private boolean enableIdempotence = true;
        
        @Positive
        private int maxInFlightRequestsPerConnection = 5;
    }

    @Data
    public static class Partitioning {
        private String strategy = "default";
        private String partitionerClass;
        private Map<String, Object> properties = new HashMap<>();
    }

    @Data
    public static class Metrics {
        private boolean enabled = true;
        private String prefix = "concord.kafka.producer";
        private Duration recordingLevel = Duration.ofSeconds(30);
        private Map<String, String> tags = new HashMap<>();
    }

    @Data
    public static class Tracing {
        private boolean enabled = true;
        private String serviceName = "kafka-producer";
        private double samplingProbability = 1.0;
    }

}