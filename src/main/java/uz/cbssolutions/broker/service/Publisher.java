package uz.cbssolutions.broker.service;

import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Map;

/**
 * Message publisher.
 */
public interface Publisher {

    /**
     * Publishes specified message.
     *
     * @param topic           Name of target topic.
     * @param model           Object to publish.
     * @param headers         Additional headers to publish.
     * @param <TRequestModel> Type of object to publish.
     * @return End operation signal.
     */
    <TRequestModel extends Serializable> Mono<Void> publish(String topic, TRequestModel model,
                                                            Map<String, Object> headers);
}
