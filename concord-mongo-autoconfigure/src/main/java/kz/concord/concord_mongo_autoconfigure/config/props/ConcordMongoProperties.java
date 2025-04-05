package kz.concord.concord_mongo_autoconfigure.config.props;

import kz.concord.concord_mongo_autoconfigure.monitoring.MongoLogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "concord.mongo")
public class ConcordMongoProperties {
    private String uri;
    private String database;
    private Credentials credentials = new Credentials();
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Credentials {
        private String username;
        private String password;
    }

    @Data
    public static class Monitoring {
        private boolean enabled = false;
        private MongoLogLevel logLevel = MongoLogLevel.DEBUG;
    }
}
