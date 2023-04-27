package uz.cbssolutions.broker.util;

import jakarta.jms.Message;
import lombok.SneakyThrows;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utilities for JMS message publisher.
 */
@Component
public class PublisherUtil {

    /**
     * Multiple headers' applier.
     *
     * @param headers Target headers.
     * @return Post processor of message.
     */
    public MessagePostProcessor applyHeaders(Map<String, Object> headers) {
        return message -> {
            if (headers != null) {
                headers.entrySet().forEach(header -> applyHeader(message, header));
            }
            return message;
        };
    }

    /**
     * Single header applier.
     *
     * @param message Target message.
     * @param header  Target header.
     */
    @SneakyThrows
    public void applyHeader(Message message, Map.Entry<String, Object> header) {
        message.setObjectProperty(header.getKey(), header.getValue());
    }
}
