package uz.cbssolutions.broker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "artemis-jms")
public class JmsProperties {
    private String url;
    private String clientId;
}
