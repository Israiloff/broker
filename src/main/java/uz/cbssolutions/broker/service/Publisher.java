package uz.cbssolutions.broker.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface Publisher {
    <TRequestModel> Mono<Void> publish(String topic, TRequestModel model, Map<String, Object> headers);
}
