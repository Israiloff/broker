package uz.cbssolutions.broker.service;

import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import uz.cbssolutions.rjvalidation.constraint.CmNotBlank;
import uz.cbssolutions.rjvalidation.constraint.CmNotNull;

import java.io.Serializable;
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
    <TRequestModel extends Serializable> Mono<Void> publish(@CmNotBlank String topic, @CmNotNull TRequestModel model,
                                                            Map<String, Object> headers);
}
