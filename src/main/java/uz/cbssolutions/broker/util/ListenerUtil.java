package uz.cbssolutions.broker.util;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.SneakyThrows;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.springframework.stereotype.Component;
import uz.cbssolutions.broker.error.MessageTypeMismatchException;

import java.util.HashMap;

@Component
public class ListenerUtil {

    @SneakyThrows
    public HashMap<String, Object> getMessageProperties(jakarta.jms.Message msg) {
        var properties = new HashMap<String, Object>();
        var srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            var propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, msg.getObjectProperty(propertyName));
        }
        return properties;
    }

    @SneakyThrows
    public String getJsonBody(jakarta.jms.Message message) {

        if (!(message instanceof TextMessage)) {
            throw new MessageTypeMismatchException(TextMessage.class);
        }

        return ((TextMessage) message).getText();
    }

    public String getTopicName(jakarta.jms.Message message) throws JMSException {
        return ((ActiveMQTopic) message.getJMSDestination()).getName();
    }
}
