package io.github.israiloff.broker.service.impl;

import io.github.israiloff.broker.config.PublisherConfig;
import io.github.israiloff.broker.service.Publisher;
import io.github.israiloff.broker.util.PublisherUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Implementation of message publisher contract.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherImpl implements Publisher {

    @Qualifier(PublisherConfig.JMS_TEMPLATE)
    private final JmsTemplate jmsTemplate;
    private final PublisherUtil publisherUtil;

    /**
     * @param topic           Name of target topic.
     * @param model           Object to publish.
     * @param headers         Additional headers to publish.
     * @param <TRequestModel> Type of publishing model (body).
     * @return End operation signal.
     */
    @Override
    public <TRequestModel> Mono<Void> publish(String topic, TRequestModel model,
                                              Map<String, Object> headers) {
        log.debug("publish started for topic : {}", topic);
        return Mono.<Void>create(sink -> {
            jmsTemplate.convertAndSend(topic, model, publisherUtil.applyHeaders(headers));
            sink.success();
        }).publishOn(Schedulers.boundedElastic());
    }
}
