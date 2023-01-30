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
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SubscriberConfig {

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;
    @Qualifier(JmsConfig.CONNECTION_FACTORY)
    private final ConnectionFactory connectionFactory;
    private final GenericApplicationContext applicationContext;

    @Bean
    public ApplicationRunner runner(List<Subscriber> subscribers, MessageListenerAdapter messageListenerAdapter) {
        return args -> subscribers.forEach(subscriber -> {
            var container = createContainer(subscriber, messageListenerAdapter);
            var beanName = "MessageListenerContainer_" + subscriber.getTopic();
            applicationContext.registerBean(beanName, DefaultMessageListenerContainer.class, () -> container);
            applicationContext.getBean(beanName, DefaultMessageListenerContainer.class).start();
        });
    }

    private DefaultMessageListenerContainer createContainer(Subscriber subscriber, MessageListenerAdapter messageListenerAdapter) {
        var container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageConverter(messageConverter);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        container.setClientId(applicationContext.getId() + "_" + UUID.randomUUID());
        container.setPubSubDomain(true);
        container.setSubscriptionDurable(true);
        container.setSubscriptionShared(true);
        container.setDestinationName(subscriber.getTopic());
        container.setMessageListener(messageListenerAdapter);
        return container;
    }
}
