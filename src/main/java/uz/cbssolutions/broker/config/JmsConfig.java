package uz.cbssolutions.broker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQTopicConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * JMS related common configurations.
 */
@SuppressWarnings("SpringFacetCodeInspection")
@EnableJms
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {JmsProperties.class})
public class JmsConfig {

    public static final String CONNECTION_FACTORY = "artemisConnectionFactory";
    public static final String MESSAGE_CONVERTER = "artemisMessageConverter";

    /**
     * Connection factory behaviour defined bean.
     *
     * @param properties JMS external properties.
     * @return Instance of configured connection factory.
     */
    @SneakyThrows
    @Bean(CONNECTION_FACTORY)
    public ConnectionFactory getConnectionFactory(JmsProperties properties) {
        log.debug("getConnectionFactory started for broker url : {}", properties.url());
        var connectionFactory = new ActiveMQTopicConnectionFactory();
        connectionFactory.setBrokerURL(properties.url());
        connectionFactory.setUser(properties.user());
        connectionFactory.setPassword(properties.password());
        return connectionFactory;
    }

    /**
     * JMS messages' serialization/deserialization rules defined bean.
     *
     * @param objectMapper Jackson mapper's instance.
     * @return Configured message converter.
     */
    @Bean(MESSAGE_CONVERTER)
    public MessageConverter jacksonJmsMessageConverter(
            @Qualifier(SerializerConfig.SERIALIZER_NAME) ObjectMapper objectMapper) {
        var converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
