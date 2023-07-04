package uz.cbssolutions.broker.util;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.springframework.stereotype.Component;
import uz.cbssolutions.broker.config.ExchangeType;
import uz.cbssolutions.broker.config.JmsProperties;
import uz.cbssolutions.broker.error.MessageTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for JMS message listener.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListenerUtil {

    private final JmsProperties properties;

    /**
     * Gets headers of message.
     *
     * @param msg Target message.
     * @return Headers containing map.
     */
    @SneakyThrows
    public Map<String, Object> getHeaders(jakarta.jms.Message msg) {
        log.debug("getHeaders started");

        var propertyMap = new HashMap<String, Object>();
        var srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            var propertyName = (String) srcProperties.nextElement();
            propertyMap.put(propertyName, msg.getObjectProperty(propertyName));
        }

        log.debug("headers created : {}", propertyMap);
        return propertyMap;
    }

    /**
     * Extracts serialized to Json format content (body) of message.
     *
     * @param message Target message.
     * @return Json representation of body.
     */
    @SneakyThrows
    public String getJsonBody(jakarta.jms.Message message) {
        log.debug("getJsonBody started");

        if (!(message instanceof TextMessage)) {
            log.error("received message is not of type {}", TextMessage.class.getName());
            throw new MessageTypeMismatchException(TextMessage.class);
        }

        var result = ((TextMessage) message).getText();

        log.debug("parsed json body : {}", result);
        return result;
    }

    /**
     * Extracts the name of topic from specified message.
     *
     * @param message Target message.
     * @return Topic name.
     * @throws JMSException JMS processing error.
     */
    public String getTopicName(jakarta.jms.Message message) throws JMSException {
        log.debug("getTopicName started");

        var result = properties.exchangeType() == ExchangeType.TOPIC
                ? ((ActiveMQTopic) message.getJMSDestination()).getName()
                : ((ActiveMQQueue) message.getJMSDestination()).getName();

        log.debug("topic name resolved : {}", result);
        return result;
    }
}
