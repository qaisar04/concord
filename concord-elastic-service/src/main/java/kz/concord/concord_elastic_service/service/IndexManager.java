package kz.concord.concord_elastic_service.service;

import reactor.core.publisher.Mono;

public interface IndexManager {
    Mono<Boolean> indexExists(String indexName);
    Mono<Void> createIndex(String indexName, String mappingJson);
    Mono<Void> deleteIndex(String indexName);
    Mono<Void> recreateIndex(String indexName, String mappingJson);
}
