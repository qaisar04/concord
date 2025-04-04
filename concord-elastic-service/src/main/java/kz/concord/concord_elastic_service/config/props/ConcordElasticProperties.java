package kz.concord.concord_elastic_service.config.props;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "concord.elastic")
public class ConcordElasticProperties {
    private String url;
    private String username;
    private String password;
    private boolean autoInit = true;
    private List<IndexConfig> indices;

    @Data
    public static class IndexConfig {
        private String name;
        private String mapping;
    }
}
