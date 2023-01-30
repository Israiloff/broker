package uz.cbssolutions.broker.service.impl;

import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uz.cbssolutions.broker.config.JmsSubPubConfig;
import uz.cbssolutions.broker.service.Publisher;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtemisPublisher implements Publisher {

    @Qualifier(JmsSubPubConfig.JMS_TEMPLATE)
    private final JmsTemplate jmsTemplate;

    private static MessagePostProcessor configureMessage(Map<String, Object> headers) {
        return message -> {
            headers.entrySet().forEach(header -> applyHeader(message, header));
            return message;
        };
    }

    @SneakyThrows
    private static void applyHeader(Message message, Map.Entry<String, Object> header) {
        message.setObjectProperty(header.getKey(), header.getValue());
    }

    @Override
    public <TRequestModel> Mono<Void> publish(String topic, TRequestModel model, Map<String, Object> headers) {
        log.debug("publish started for topic : {}", topic);
        return Mono.<Void>create(sink -> {
            jmsTemplate.convertAndSend(topic, model, configureMessage(headers));
            sink.success();
        }).publishOn(Schedulers.boundedElastic());
    }
}
