package kz.concord.concord_kafka_producer.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcordKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T extends SpecificRecordBase> Mono<Void> send(String topic, String key, T message) {
        return send(topic, key, Collections.singletonList(message));
    }

    public <T extends SpecificRecordBase> Mono<Void> send(String topic, String key, List<T> messages) {
        List<Mono<SendResult<String, Object>>> sendMonos = messages.stream()
                .map(message -> Mono.fromFuture(sendAsync(topic, key, message)))
                .collect(Collectors.toList());

        return Mono.when(sendMonos)
                .doOnSuccess(unused -> log.debug("Successfully sent {} messages to topic {}", messages.size(), topic))
                .doOnError(e -> log.error("Failed to send messages to topic {}: {}", topic, e.getMessage(), e))
                .then();
    }

    private <T extends SpecificRecordBase> CompletableFuture<SendResult<String, Object>> sendAsync(
            String topic, String key, T message
    ) {
        log.debug("Sending message to topic {} with key {}: {}", topic, key, message);
        return kafkaTemplate.send(topic, key, message);
    }
}
