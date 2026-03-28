package com.gym.system.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq-enabled", havingValue = "true")
public class RabbitConfig {
    @Bean
    public DirectExchange gymSyncExchange() {
        return new DirectExchange("gym.sync.exchange", true, false);
    }

    @Bean
    public Queue gymSyncQueue() {
        return new Queue("gym.sync.queue", true);
    }

    @Bean
    public Binding gymSyncBinding(Queue gymSyncQueue, DirectExchange gymSyncExchange) {
        return BindingBuilder.bind(gymSyncQueue).to(gymSyncExchange).with("gym.sync.route");
    }
}
