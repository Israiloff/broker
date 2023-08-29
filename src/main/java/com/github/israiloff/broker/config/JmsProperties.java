package com.github.israiloff.broker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Main JMS external properties.
 *
 * @param url          Address of message broker (ex. {@code tcp://localhost:61616}).
 * @param user         Username for passing message broker's security procedure.
 * @param password     Password of {@code user}.
 * @param exchangeType Type of exchange.
 */
@ConfigurationProperties(prefix = "com.github.israiloff.broker")
public record JmsProperties(String url, String user, String password, ExchangeType exchangeType) {
}
