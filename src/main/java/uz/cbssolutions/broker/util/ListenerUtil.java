package uz.cbssolutions.broker.util;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.apache.commons.collections.IteratorUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uz.cbssolutions.broker.config.ExchangeType;
import uz.cbssolutions.broker.config.JmsProperties;
import uz.cbssolutions.broker.error.GetHeadersException;
import uz.cbssolutions.broker.error.GetMessageException;
import uz.cbssolutions.broker.error.HeaderExtractionException;
import uz.cbssolutions.broker.error.MessageTypeMismatchException;
import uz.cbssolutions.broker.error.TopicNameResolveException;
import uz.cbssolutions.broker.model.KeyPair;

import java.util.Enumeration;
import java.util.Map;

/**
 * Utilities for JMS message listener.
 */
@SuppressWarnings("rawtypes")
@Slf4j
@Component
@RequiredArgsConstructor
public class ListenerUtil {

    private final JmsProperties properties;

    /**
     * Gets headers of message.
     *
     * @param msg Target message.
     * @return Headers containing map.
     */
    @SneakyThrows
    public Mono<Map<String, Object>> getHeaders(jakarta.jms.Message msg) {

        return Mono.<Enumeration>create(sink -> {
                    log.debug("getHeaders started");
                    try {
                        sink.success(msg.getPropertyNames());
                    } catch (JMSException e) {
                        sink.error(new GetHeadersException(e));
                    }
                })
                .map(Enumeration::asIterator)
                .<String>flatMapIterable(IteratorUtils::toList)
                .<KeyPair<String, Object>>handle((propertyName, sink) -> {
                    try {
                        sink.next(new KeyPair<>(propertyName, msg.getObjectProperty(propertyName)));
                    } catch (Throwable e) {
                        sink.error(new HeaderExtractionException(e));
                    }
                })
                .collectMap(KeyPair::key, KeyPair::value)
                .publishOn(Schedulers.boundedElastic());
    }

    /**
     * Extracts serialized to Json format content (body) of message.
     *
     * @param message Target message.
     * @return Json representation of body.
     */
    @SneakyThrows
    public Mono<String> getJsonBody(jakarta.jms.Message message) {
        return Mono.<TextMessage>create(sink -> {
                    log.debug("getJsonBody started");

                    if (!(message instanceof TextMessage)) {
                        log.error("received message is not of type {}", TextMessage.class.getName());
                        sink.error(new MessageTypeMismatchException(TextMessage.class));
                        return;
                    }

                    sink.success(((TextMessage) message));
                })
                .<String>handle((msg, sink) -> {
                    try {
                        sink.next(msg.getText());
                    } catch (JMSException e) {
                        sink.error(new GetMessageException(e));
                    }
                })
                .doOnNext(json -> log.debug("parsed json body : {}", json))
                .publishOn(Schedulers.boundedElastic());
    }

    /**
     * Extracts the name of topic from specified message.
     *
     * @param message Target message.
     * @return Topic name.
     */
    public Mono<String> getTopicName(jakarta.jms.Message message) {
        return Mono.<String>create(sink -> {
                    log.debug("getTopicName started");
                    try {
                        var result = properties.exchangeType() == ExchangeType.TOPIC
                                ? ((ActiveMQTopic) message.getJMSDestination()).getName()
                                : ((ActiveMQQueue) message.getJMSDestination()).getName();
                        sink.success(result);
                    } catch (Throwable e) {
                        sink.error(new TopicNameResolveException(e));
                    }
                })
                .doOnNext(result -> log.debug("topic name resolved : {}", result))
                .publishOn(Schedulers.boundedElastic());
    }
}
