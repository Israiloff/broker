package io.github.israiloff.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Subscriber's default properties.
 *
 * @param url      Url address of the target message broker server.
 * @param user     Username of the target message broker server.
 * @param password Password of the target message broker server.
 */
@ConfigurationProperties(prefix = "io.github.israiloff.broker.subscriber")
public record SubscriberProperties(String url, String user, String password) {
}
