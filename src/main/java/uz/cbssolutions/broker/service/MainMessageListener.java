package uz.cbssolutions.broker.service;

import jakarta.jms.MessageListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uz.cbssolutions.broker.error.SubscriberNotFoundException;
import uz.cbssolutions.broker.model.Message;
import uz.cbssolutions.broker.util.ListenerUtil;
import uz.cbssolutions.broker.util.SerializationUtil;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class MainMessageListener implements MessageListener {

    private final Flux<Subscriber> subscribers;
    private final SerializationUtil serializationUtil;
    private final ListenerUtil listenerUtil;

    @SneakyThrows
    public MainMessageListener(List<Subscriber> subscribers, SerializationUtil serializationUtil,
                               ListenerUtil listenerUtil) {
        if (subscribers.size() < 1) {
            throw new SubscriberNotFoundException();
        }

        this.serializationUtil = serializationUtil;
        this.subscribers = Flux.fromIterable(subscribers);
        this.listenerUtil = listenerUtil;
    }

    @SneakyThrows
    @Override
    public void onMessage(jakarta.jms.Message message) {
        log.debug("onMessage started");

        var topic = listenerUtil.getTopicName(message);
        var json = listenerUtil.getJsonBody(message);
        var headers = listenerUtil.getMessageProperties(message);
        var filtered = subscribers.filter(subscriber -> Objects.equals(subscriber.getTopic(), topic));

        filtered.last()
                .map(subscriber -> serializationUtil.deserialize(json, subscriber.getMsgClass()))
                .flatMapMany(o -> filtered.flatMap(subscriber -> subscriber.handle(new Message(o, headers))))
                .doOnError(throwable -> log.error("error occurred while processing jms message : {}", throwable))
                .subscribe();
    }
}
