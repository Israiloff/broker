package uz.cbssolutions.broker.service;

import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.springframework.stereotype.Component;
import uz.cbssolutions.broker.model.Message;
import uz.cbssolutions.broker.util.SerializationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class MainMessageListener implements MessageListener {

    private final Map<String, List<Subscriber>> subscribers;
    private final SerializationUtil serializationUtil;

    public MainMessageListener(List<Subscriber> subscribers, SerializationUtil serializationUtil) {
        this.serializationUtil = serializationUtil;
        this.subscribers = subscribers
                .stream()
                .collect(Collectors.groupingBy(Subscriber::getTopic));
    }

    private static HashMap<String, Object> getMessageProperties(jakarta.jms.Message msg) throws JMSException {
        var properties = new HashMap<String, Object>();
        var srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            var propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, msg.getObjectProperty(propertyName));
        }
        return properties;
    }

    @SneakyThrows
    @Override
    public void onMessage(jakarta.jms.Message message) {
        log.debug("onMessage started");
        if (message instanceof TextMessage) {
            var jsonText = ((TextMessage) message).getText();
            var topic = ((ActiveMQTopic) message.getJMSDestination()).getName();
            var body = serializationUtil.deserialize(jsonText, subscribers.get(topic).stream().findFirst().orElseThrow().getMsgClass());
            var headers = getMessageProperties(message);
            subscribers.get(topic).forEach(subscriber -> subscriber.handle(new Message(body, headers)));
//            var subscribersPublisher = Flux.fromIterable(subscribers.get(topic));
//
//            subscribersPublisher
//                    .last()
//                    .map(Subscriber::getMsgClass)
//                    .flatMap(targetClass -> serializationUtil.reactiveDeserialization(jsonText, targetClass))
//                    .log()
//                    .flatMapMany(o -> subscribersPublisher.flatMap(subscriber -> subscriber.handle(new Message(o, new HashMap<>()))))
//                    .doOnError(throwable -> log.error("error occurred while processing jms message : {}", throwable))
//                    .subscribe();
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }
}
