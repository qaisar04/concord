package kz.concord.concord_mongo_autoconfigure.config;

import kz.concord.concord_mongo_autoconfigure.config.props.ConcordMongoProperties;
import kz.concord.concord_mongo_autoconfigure.monitoring.MongoLoggingCommandListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ConcordMongoProperties.class)
public class MongoMonitoringConfiguration {

    private final ConcordMongoProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "concord.mongo.monitoring", name = "enabled", havingValue = "true")
    public MongoClientSettingsBuilderCustomizer monitoringCustomizer() {
        return clientSettingsBuilder -> clientSettingsBuilder
                .addCommandListener(new MongoLoggingCommandListener(properties.getMonitoring().getLogLevel()));
    }
}
