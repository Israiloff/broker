package io.github.israiloff.broker.service;

import io.github.israiloff.broker.config.ExchangeType;
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
     * Defines the name of the sub queue.
     *
     * @return Name of the sub queue.
     */
    default String getSubQueue() {
        return null;
    }

    /**
     * Defines unique ID of the current JMS client.
     * By default, value of the client ID will be the service name + random UUID.
     * <p>
     * E.g. dummy-service_3a187832-9d66-11ee-8c90-0242ac120002
     *
     * @return ID of JMS client.
     */
    default String getClientId() {
        return null;
    }

    /**
     * Enables subscription durability feature.
     * Note that this feature works only for {@link #getExchangeType() exchange type} -
     * {@link ExchangeType#TOPIC TOPIC}.
     * Disabled by default.
     *
     * @return Durable flag.
     */
    default Boolean isDurable() {
        return false;
    }

    /**
     * Enables shared subscription feature.
     * Note that this feature works only for {@link #getExchangeType() exchange type} -
     * {@link ExchangeType#TOPIC TOPIC}.
     * Disabled by default.
     *
     * @return Shared subscription flag.
     */
    default Boolean isShared() {
        return false;
    }

    /**
     * Defines a type of exchange of this subscription.
     * Default value is {@link ExchangeType#TOPIC TOPIC}
     *
     * @return Exchange type of this subscription.
     */
    default ExchangeType getExchangeType() {
        return ExchangeType.TOPIC;
    }

    /**
     * Incoming message handler.
     *
     * @param message Incoming message.
     * @return End operation signal.
     */
    Mono<Void> handle(Message<TRequestModel> message);
}
