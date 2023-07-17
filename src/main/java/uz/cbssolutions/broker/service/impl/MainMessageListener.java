package uz.cbssolutions.broker.service.impl;

import jakarta.jms.MessageListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uz.cbssolutions.broker.model.KeyPair;
import uz.cbssolutions.broker.model.Message;
import uz.cbssolutions.broker.model.RequestData;
import uz.cbssolutions.broker.service.Subscriber;
import uz.cbssolutions.broker.util.ListenerUtil;
import uz.cbssolutions.serializer.service.SneakySerializer;

import java.io.Serializable;
import java.util.List;
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
                                .<KeyPair<Subscriber, Serializable>>handle((subscriber, sink) -> {
                                    var result = (Serializable) serializer
                                            .deserialize(data.jsonBody(), subscriber.getMsgClass());
                                    sink.next(new KeyPair<>(subscriber, result));
                                })
                                .flatMap(pair -> pair.key().handle(new Message(pair.value(), data.headers()))))
                .doOnError(e -> log.error("error occurred while processing jms message", e))
                .subscribe();
    }
}
