package kz.concord.concord_elastic_service.service.impl;

import co.elastic.clients.transport.endpoints.BooleanResponse;
import java.io.StringReader;
import kz.concord.concord_elastic_service.service.IndexManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IndexManagerImpl implements IndexManager {

    private final ReactiveElasticsearchClient client;

    @Override
    public Mono<Boolean> indexExists(String indexName) {
        return client.indices()
                .exists(req -> req.index(indexName))
                .map(BooleanResponse::value);
    }

    @Override
    public Mono<Void> createIndex(String indexName, String mappingJson) {
        return client.indices()
                .create(c -> c
                        .index(indexName)
                        .withJson(new StringReader(mappingJson))
                ).then();
    }

    @Override
    public Mono<Void> deleteIndex(String indexName) {
        return client.indices()
                .delete(d -> d.index(indexName))
                .then();
    }

    @Override
    public Mono<Void> recreateIndex(String indexName, String mappingJson) {
        return deleteIndex(indexName)
                .then(createIndex(indexName, mappingJson));
    }
}

