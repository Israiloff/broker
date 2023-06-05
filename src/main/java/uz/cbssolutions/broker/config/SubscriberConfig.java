package uz.cbssolutions.broker.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConverter;
import uz.cbssolutions.broker.service.Subscriber;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Subscription related specific beans configuration.
 */
@SuppressWarnings({"rawtypes", "SpringFacetCodeInspection"})
@Configuration
@RequiredArgsConstructor
public class SubscriberConfig {

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;
    @Qualifier(JmsConfig.CONNECTION_FACTORY)
    private final ConnectionFactory connectionFactory;
    private final GenericApplicationContext applicationContext;

    /**
     * Multiple JMS message listener container beans creation point. Created message listener containers' count will be
     * equal to implementations of {@link Subscriber} (i.e. each {@link Subscriber} will have its own container).
     *
     * @param subscribers            List of implemented {@link Subscriber}.
     * @param messageListenerAdapter Default message listener adapter.
     * @param properties             JMS properties.
     * @return Runner's bean.
     */
    @Bean
    public ApplicationRunner runner(List<Subscriber> subscribers, MessageListenerAdapter messageListenerAdapter,
                                    JmsProperties properties) {
        return args -> subscribers.forEach(subscriber -> {
            var container = createContainer(messageListenerAdapter, subscriber, properties);
            var beanName = "messageListenerContainer_" + subscriber.getTopic();
            applicationContext.registerBean(beanName, DefaultMessageListenerContainer.class, () -> container);
            container.start();
        });
    }

    private DefaultMessageListenerContainer createContainer(MessageListenerAdapter messageListenerAdapter,
                                                            Subscriber subscriber, JmsProperties properties) {
        var container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageConverter(messageConverter);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        container.setClientId(applicationContext.getId() + "_" + UUID.randomUUID());
        container.setPubSubDomain(Objects.equals(properties.exchangeType(), ExchangeType.TOPIC));
        container.setDestinationName(subscriber.getTopic());
        container.setMessageListener(messageListenerAdapter);
        return container;
    }
}
