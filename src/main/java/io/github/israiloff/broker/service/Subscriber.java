package io.github.israiloff.broker.service;

import io.github.israiloff.broker.model.Message;
import reactor.core.publisher.Mono;

/**
 * Message subscriber.
 * Subscribes to specified topic.
 *
 * @param <TRequestModel> Type of expected incoming message (i.e. Body of message).
 */
public interface Subscriber<TRequestModel> {

    /**
     * Method to get class of expected body.
     *
     * @return Expected class.
     */
    Class<TRequestModel> getMsgClass();

    /**
     * Gets name of subscribed topic.
     *
     * @return Topic name.
     */
    String getTopic();

    /**
     * Incoming message handler.
     *
     * @param message Incoming message.
     * @return End operation signal.
     */
    Mono<Void> handle(Message<TRequestModel> message);
}

