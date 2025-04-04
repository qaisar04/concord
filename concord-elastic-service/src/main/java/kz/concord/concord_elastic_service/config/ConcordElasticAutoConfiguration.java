package kz.concord.concord_elastic_service.config;

import kz.concord.concord_elastic_service.config.props.ConcordElasticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@Configuration
@RequiredArgsConstructor
@EnableReactiveElasticsearchRepositories
@EnableConfigurationProperties(ConcordElasticProperties.class)
public class ConcordElasticAutoConfiguration extends ReactiveElasticsearchConfiguration {

    private final ConcordElasticProperties properties;

    @Override
    public ClientConfiguration clientConfiguration() {
        if (properties.getUsername() != null && properties.getPassword() != null) {
            return ClientConfiguration.builder()
                    .connectedTo(properties.getUrl().replace("http://", "").replace("https://", ""))
                    .withBasicAuth(properties.getUsername(), properties.getPassword())
                    .build();
        }

        return ClientConfiguration.builder()
                .connectedTo(properties.getUrl().replace("http://", "").replace("https://", ""))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveElasticsearchConfiguration.class)
    public ReactiveElasticsearchConfiguration elasticConfig() {
        return new ConcordElasticAutoConfiguration(properties);
    }
}
