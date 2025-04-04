package kz.concord.concord_elastic_service.service;

import org.springframework.data.elasticsearch.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentService<T> {
    Mono<Void> index(String index, String id, T document);
    Mono<T> getById(String index, String id, Class<T> type);
    Flux<T> search(Query query, Class<T> type);
    Mono<Void> delete(String index, String id);
}
