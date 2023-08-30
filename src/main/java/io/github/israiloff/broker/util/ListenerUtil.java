package io.github.israiloff.broker.util;

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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import io.github.israiloff.broker.config.ExchangeType;
import io.github.israiloff.broker.config.JmsProperties;
import com.github.israiloff.broker.error.GetHeadersException;
import com.github.israiloff.broker.error.GetMessageException;
import com.github.israiloff.broker.error.HeaderExtractionException;
import com.github.israiloff.broker.error.MessageTypeMismatchException;
import com.github.israiloff.broker.error.TopicNameResolveException;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Utilities for JMS message listener.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
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
                .flatMapIterable(iterator -> (List<String>) IteratorUtils.toList(iterator))
                .<Tuple2<String, Object>>handle((propertyName, sink) -> {
                    try {
                        sink.next(Tuples.of(propertyName, msg.getObjectProperty(propertyName)));
                    } catch (Throwable e) {
                        sink.error(new HeaderExtractionException(e));
                    }
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2)
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
