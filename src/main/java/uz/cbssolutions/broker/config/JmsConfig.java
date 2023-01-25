package uz.cbssolutions.broker.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQTopicConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@EnableJms
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {JmsProperties.class})
public class JmsConfig {

    public static final String CONNECTION_FACTORY = "artemisConnectionFactory";
    public static final String MESSAGE_CONVERTER = "artemisMessageConverter";

    private final JmsProperties properties;

    @SneakyThrows
    @Bean(CONNECTION_FACTORY)
    public ConnectionFactory getConnectionFactory() {
        log.debug("getConnectionFactory started for broker url : {}", properties.getUrl());
        var connectionFactory = new ActiveMQTopicConnectionFactory();
        connectionFactory.setBrokerURL(properties.getUrl());
        return connectionFactory;
    }

    @Bean(MESSAGE_CONVERTER)
    public MessageConverter jacksonJmsMessageConverter() {
        var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
