package uz.cbssolutions.broker.service;

import reactor.core.publisher.Mono;
import uz.cbssolutions.broker.model.Message;

public interface Subscriber<TMessage> {
    Class<TMessage> getMsgClass();

    String getTopic();

    Mono<Void> handle(Message<TMessage> message);
}

