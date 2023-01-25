package uz.cbssolutions.broker.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {JmsProperties.class})
public class JmsSubPubConfig {

    public static final String CONTAINER_FACTORY = "cmArtemisJmsContainerFactory";
    public static final String JMS_TEMPLATE = "cmArtemisJmsTemplate";

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;
    @Qualifier(JmsConfig.CONNECTION_FACTORY)
    private final ConnectionFactory connectionFactory;

    //    for publisher
    @Bean(JMS_TEMPLATE)
    public JmsTemplate artemisJmsTemplate() {
        var template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setPubSubDomain(true);
        return template;
    }

    //    for consumer
    @Bean(CONTAINER_FACTORY)
    public JmsListenerContainerFactory<?> artemisJmsContainerFactory(DefaultJmsListenerContainerFactoryConfigurer configurer, JmsProperties properties) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setMessageConverter(messageConverter);
        // TODO: 6/13/2022 - fix retries on error
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setClientId(properties.getClientId());
        factory.setPubSubDomain(true);
        factory.setSubscriptionDurable(true);
        factory.setSubscriptionShared(true);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
