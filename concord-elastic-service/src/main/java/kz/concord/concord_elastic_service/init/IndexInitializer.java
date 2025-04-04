package kz.concord.concord_elastic_service.init;

import jakarta.annotation.PostConstruct;
import kz.concord.concord_elastic_service.config.props.ConcordElasticProperties;
import kz.concord.concord_elastic_service.init.loader.MappingLoader;
import kz.concord.concord_elastic_service.service.IndexManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexInitializer {

    private final ConcordElasticProperties properties;
    private final IndexManager indexManager;

    @PostConstruct
    public void init() {
        if (!properties.isAutoInit()) {
            log.info("Auto index initialization disabled.");
            return;
        }

        if (properties.getIndices() == null || properties.getIndices().isEmpty()) {
            log.info("No indices configured for initialization.");
            return;
        }

        for (ConcordElasticProperties.IndexConfig indexConfig : properties.getIndices()) {
            String indexName = indexConfig.getName();
            String mappingPath = indexConfig.getMapping();

            log.info("Initializing index '{}'", indexName);

            indexManager.indexExists(indexName)
                    .flatMap(exists -> {
                        if (!exists) {
                            String mappingJson = MappingLoader.loadMapping(mappingPath);
                            return indexManager.createIndex(indexName, mappingJson);
                        }
                        return Mono.empty();
                    })
                    .doOnSuccess(x -> log.info("Index '{}' initialized", indexName))
                    .doOnError(err -> log.error("Failed to initialize index '{}': {}", indexName, err.getMessage()))
                    .subscribe();
        }
    }
}
