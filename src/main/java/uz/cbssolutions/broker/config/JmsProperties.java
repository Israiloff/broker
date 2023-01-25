package uz.cbssolutions.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cbs-broker")
public record JmsProperties(String url, String user, String password) {
}
