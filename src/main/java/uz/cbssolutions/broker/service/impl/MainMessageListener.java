package uz.cbssolutions.broker.service.impl;

import jakarta.jms.MessageListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;
import uz.cbssolutions.broker.error.JmsMessageDeserializationException;
import uz.cbssolutions.broker.model.Message;
import uz.cbssolutions.broker.model.RequestData;
import uz.cbssolutions.broker.service.Subscriber;
import uz.cbssolutions.broker.util.ListenerUtil;
import uz.cbssolutions.serializer.service.SneakySerializer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entry point of all incoming messages. Used as resolver of subscribed topics.
 */
@Slf4j
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class MainMessageListener implements MessageListener {

    private final Flux<Subscriber> subscribers;
    private final SneakySerializer serializer;
    private final ListenerUtil listenerUtil;

    @SneakyThrows
    public MainMessageListener(List<Subscriber> subscribers, SneakySerializer serializer, ListenerUtil listenerUtil) {
        this.serializer = serializer;
        this.subscribers = Flux.fromIterable(subscribers);
        this.listenerUtil = listenerUtil;
    }

    /**
     * Handler of incoming messages.
     *
     * @param message The message passed to the listener
     */
    @SneakyThrows
    @Override
    public void onMessage(jakarta.jms.Message message) {
        log.debug("onMessage started");

        listenerUtil.getTopicName(message)
                .zipWith(listenerUtil.getJsonBody(message))
                .zipWith(listenerUtil.getHeaders(message))
                .map(objects -> new RequestData(objects.getT1().getT1(), objects.getT1().getT2(), objects.getT2()))
                .flatMap(data ->
                        subscribers
                                .filter(subscriber -> Objects.equals(subscriber.getTopic(), data.topic()))
                                .last()
                                .<Tuple3<Subscriber, Serializable, Map<String, Object>>>handle(
                                        (subscriber, sink) -> {
                                            try {
                                                var result = (Serializable) serializer
                                                        .deserialize(data.jsonBody(), subscriber.getMsgClass());
                                                sink.next(Tuples.of(subscriber, result, data.headers()));

                                            } catch (Throwable e) {
                                                sink.error(new JmsMessageDeserializationException(e));
                                            }
                                        })
                )
                .<Void>flatMap(tuple3 -> tuple3.getT1().handle(new Message(tuple3.getT2(), tuple3.getT3())))
                .doOnError(e -> log.error("error occurred while processing jms message", e))
                .subscribe();
    }
}
