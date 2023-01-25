package uz.cbssolutions.broker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uz.cbssolutions.broker.config.JmsSubPubConfig;
import uz.cbssolutions.broker.service.Publisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtemisPublisher implements Publisher {

    @Qualifier(JmsSubPubConfig.JMS_TEMPLATE)
    private final JmsTemplate jmsTemplate;

    @Override
    public Mono<Void> publish(String topic, Object object) {
        log.debug("publish started for topic : {}", topic);
        return Mono.<Void>create(sink -> {
            jmsTemplate.convertAndSend(topic, object);
            sink.success();
        }).publishOn(Schedulers.boundedElastic());
    }
}
