package io.github.israiloff.broker.util;

import io.github.israiloff.broker.config.ExchangeType;
import io.github.israiloff.broker.config.SubscriberProperties;
import io.github.israiloff.broker.service.Subscriber;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopicConnectionFactory;
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

    /**
     * Creates Active MQ connection factory.
     *
     * @param exchangeType Type of exchange (QUEUE, TOPIC).
     * @return Connection factory.
     */
    @SneakyThrows
    public ConnectionFactory createConnectionFactory(ExchangeType exchangeType,
                                                     SubscriberProperties properties,
                                                     Subscriber subscriber) {
        var connectionFactory = Objects.equals(exchangeType, ExchangeType.TOPIC)
                ? new ActiveMQTopicConnectionFactory()
                : new ActiveMQQueueConnectionFactory();
        connectionFactory.setBrokerURL(subscriber.getUrl() != null ? subscriber.getUrl() : properties.url());
        connectionFactory.setUser(subscriber.getUser() != null ? subscriber.getUser() : properties.user());
        connectionFactory.setPassword(
                subscriber.getPassword() != null
                        ? subscriber.getPassword()
                        : properties.password());
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
                                                          Subscriber subscriber,
                                                          GenericApplicationContext applicationContext,
                                                          MessageConverter messageConverter,
                                                          SubscriberProperties properties) {
        var container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(
                createConnectionFactory(subscriber.getExchangeType(), properties, subscriber));
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
