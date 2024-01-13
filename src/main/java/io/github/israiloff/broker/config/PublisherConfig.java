package io.github.israiloff.broker.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.Objects;

/**
 * JMS subscriber/publisher related configurations.
 */
@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
@RequiredArgsConstructor
@ConfigurationPropertiesScan
@EnableConfigurationProperties(value = {JmsProperties.class})
public class PublisherConfig {

    /**
     * Name of the artemis JMS template bean.
     */
    public static final String JMS_TEMPLATE = "cmArtemisJmsTemplate";

    @Qualifier(JmsConfig.MESSAGE_CONVERTER)
    private final MessageConverter messageConverter;
    @Qualifier(JmsConfig.CONNECTION_FACTORY)
    private final ConnectionFactory connectionFactory;

    /**
     * Main JMS template creation defined bean.
     *
     * @param properties JMS properties.
     * @return Configured JMS template.
     */
    @Bean(JMS_TEMPLATE)
    public JmsTemplate jmsTemplate(JmsProperties properties) {
        var template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setPubSubDomain(Objects.equals(properties.exchangeType(), ExchangeType.TOPIC));
        return template;
    }
}
