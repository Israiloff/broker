package uz.cbssolutions.broker.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * JMS subscriber/publisher related configurations.
 */
@Configuration
@RequiredArgsConstructor
@ConfigurationPropertiesScan
@EnableConfigurationProperties(value = {JmsProperties.class})
public class JmsSubPubConfig {

    public static final String JMS_TEMPLATE = "cmBrokerJmsTemplate";

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;
    @Qualifier(JmsConfig.CONNECTION_FACTORY)
    private final ConnectionFactory connectionFactory;

    /**
     * Main JMS template creation defined bean.
     *
     * @return Configured JMS template.
     */
    @Bean(JMS_TEMPLATE)
    public JmsTemplate jmsTemplate() {
        var template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setPubSubDomain(true);
        return template;
    }

    /**
     * Default message listener adapter defined bean. Used for subscription purposes.
     *
     * @param listener Target message listener.
     * @return Configured message listener adapter.
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageListener listener) {
        var adapter = new MessageListenerAdapter(listener);
        adapter.setDefaultListenerMethod("onMessage");
        return adapter;
    }
}
