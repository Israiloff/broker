package uz.cbssolutions.broker.service;

import jakarta.jms.MessageListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uz.cbssolutions.broker.error.SubscriberNotFoundException;
import uz.cbssolutions.broker.model.Message;
import uz.cbssolutions.broker.util.ListenerUtil;
import uz.cbssolutions.serializer.service.SneakySerializer;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class MainMessageListener implements MessageListener {

    private final Flux<Subscriber> subscribers;
    private final SneakySerializer serializer;
    private final ListenerUtil listenerUtil;

    @SneakyThrows
    public MainMessageListener(List<Subscriber> subscribers, SneakySerializer serializer, ListenerUtil listenerUtil) {
        if (subscribers.size() < 1) {
            throw new SubscriberNotFoundException();
        }

        this.serializer = serializer;
        this.subscribers = Flux.fromIterable(subscribers);
        this.listenerUtil = listenerUtil;
    }

    @SneakyThrows
    @Override
    public void onMessage(jakarta.jms.Message message) {
        log.debug("onMessage started");

        var topic = listenerUtil.getTopicName(message);
        var json = listenerUtil.getJsonBody(message);
        var headers = listenerUtil.getHeaders(message);
        var filtered = subscribers.filter(subscriber -> Objects.equals(subscriber.getTopic(), topic));

        filtered.last()
                .map(subscriber -> serializer.deserialize(json, subscriber.getMsgClass()))
                .flatMapMany(o -> filtered.flatMap(subscriber -> subscriber.handle(new Message(o, headers))))
                .doOnError(throwable -> log.error("error occurred while processing jms message : {}", throwable))
                .subscribe();
    }
}
