package io.github.israiloff.broker.service;

import io.github.israiloff.rjvalidation.constraint.CmNotBlank;
import io.github.israiloff.rjvalidation.constraint.CmNotNull;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Message publisher.
 */
@Validated
public interface Publisher {

    /**
     * Publishes specified message.
     *
     * @param topic           Name of target topic.
     * @param model           Object to publish.
     * @param headers         Additional headers to publish. Can be null.
     * @param <TRequestModel> Type of object to publish.
     * @return End operation signal.
     */
    <TRequestModel> Mono<Void> publish(@CmNotBlank String topic, @CmNotNull TRequestModel model,
                                       Map<String, Object> headers);
}
