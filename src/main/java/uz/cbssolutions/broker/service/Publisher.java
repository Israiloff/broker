package uz.cbssolutions.broker.service;

import reactor.core.publisher.Mono;

public interface Publisher {
    Mono<Void> publish(String topic, Object object);
}
