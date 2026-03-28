package com.gym.system.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rabbitmq-enabled", havingValue = "false", matchIfMissing = true)
public class LocalSyncPublisher implements SyncPublisher {
    @Override
    public void publish(String actionType, String payload) {
        // Local fallback when RabbitMQ is not enabled.
    }
}
