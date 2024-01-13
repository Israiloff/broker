package io.github.israiloff.broker.util;

import io.github.israiloff.broker.config.ExchangeType;
import io.github.israiloff.broker.config.JmsConfig;
import io.github.israiloff.broker.config.JmsProperties;
import io.github.israiloff.broker.service.Subscriber;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopicConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * Subscriber logic related utils.
 */
@SuppressWarnings({"rawtypes"})
@Component
@RequiredArgsConstructor
public class SubscriberUtil {

    private final JmsProperties properties;

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;

    private final GenericApplicationContext applicationContext;

    /**
     * Creates Active MQ connection factory.
     *
     * @param exchangeType Type of exchange (QUEUE, TOPIC).
     * @return Connection factory.
     */
    @SneakyThrows
    public ConnectionFactory createConnectionFactory(ExchangeType exchangeType) {
        var connectionFactory = Objects.equals(exchangeType, ExchangeType.TOPIC)
                ? new ActiveMQTopicConnectionFactory()
                : new ActiveMQQueueConnectionFactory();
        connectionFactory.setBrokerURL(properties.url());
        connectionFactory.setUser(properties.user());
        connectionFactory.setPassword(properties.password());
        return connectionFactory;
    }

    /**
     * Initializes publish/subscribe strategy.
     *
     * @param subscriber Instance of the Subscriber.
     * @param container  Message listener container.
     */
    public void setupPubSubStrategy(Subscriber subscriber, SimpleMessageListenerContainer container) {
        container.setPubSubDomain(true);

        if (subscriber.isDurable()) {
            container.setSubscriptionDurable(true);
        }

        if (StringUtils.hasText(subscriber.getSubQueue())) {
            container.setSubscriptionName(subscriber.getSubQueue());
            if (subscriber.isDurable()) {
                container.setDurableSubscriptionName(subscriber.getSubQueue());
            }
            container.setClientId(subscriber.getSubQueue());
        }

        container.setSubscriptionShared(subscriber.isShared());
    }

    /**
     * Creates message listener container.
     *
     * @param messageListenerAdapter Adapter of message listener.
     * @param subscriber             Instance of the Subscriber.
     * @return Message listener container.
     */
    public SimpleMessageListenerContainer createContainer(MessageListenerAdapter messageListenerAdapter,
                                                          Subscriber subscriber) {
        var container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(createConnectionFactory(subscriber.getExchangeType()));
        container.setMessageConverter(messageConverter);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        container.setClientId(
                StringUtils.hasText(subscriber.getClientId())
                        ? subscriber.getClientId()
                        : applicationContext.getId() + "_" + UUID.randomUUID());
        container.setDestinationName(subscriber.getTopic());
        container.setMessageListener(messageListenerAdapter);

        if (Objects.equals(subscriber.getExchangeType(), ExchangeType.TOPIC)) {
            setupPubSubStrategy(subscriber, container);
        }

        return container;
    }
}
