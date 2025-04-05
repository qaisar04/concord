package kz.concord.concord_mongo_autoconfigure.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import kz.concord.concord_mongo_autoconfigure.config.props.ConcordMongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
@EnableConfigurationProperties(ConcordMongoProperties.class)
public class ConcordMongoAutoConfiguration {

    @Bean
    public MongoClient mongoClient(ConcordMongoProperties properties) {
        StringBuilder uriBuilder = new StringBuilder("mongodb://");

        if (properties.getCredentials().getUsername() != null && properties.getCredentials().getPassword() != null) {
            uriBuilder.append(properties.getCredentials().getUsername())
                    .append(":")
                    .append(properties.getCredentials().getPassword())
                    .append("@");
        }

        uriBuilder.append(properties.getUri());

        return MongoClients.create(uriBuilder.toString());
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient, ConcordMongoProperties properties) {
        return new ReactiveMongoTemplate(mongoClient, properties.getDatabase());
    }

}
