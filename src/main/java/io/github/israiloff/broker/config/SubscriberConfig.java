package io.github.israiloff.broker.config;

import io.github.israiloff.broker.service.Subscriber;
import io.github.israiloff.broker.util.SubscriberUtil;
import jakarta.jms.MessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import java.util.List;

/**
 * Subscription related specific beans configuration.
 */
@SuppressWarnings({"rawtypes", "SpringFacetCodeInspection"})
@Configuration
@RequiredArgsConstructor
public class SubscriberConfig {

    /**
     * Multiple JMS message listener container beans creation point. Created message listener containers' count will be
     * equal to implementations of {@link Subscriber} (i.e. each {@link Subscriber} will have its own container).
     *
     * @param subscribers            List of implemented {@link Subscriber}.
     * @param messageListenerAdapter Default message listener adapter.
     * @param subscriberUtil         Subscriber logic related utils.
     * @param applicationContext     Spring application context instance.
     * @return Runner's bean.
     */
    @Bean
    public ApplicationRunner runner(List<Subscriber> subscribers, MessageListenerAdapter messageListenerAdapter,
                                    SubscriberUtil subscriberUtil, GenericApplicationContext applicationContext) {
        return args -> subscribers.forEach(subscriber -> {
            var container = subscriberUtil.createContainer(messageListenerAdapter, subscriber);
            var beanName = "messageListenerContainer_" + subscriber.getTopic();
            applicationContext.registerBean(beanName, SimpleMessageListenerContainer.class, () -> container);
            container.start();
        });
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
