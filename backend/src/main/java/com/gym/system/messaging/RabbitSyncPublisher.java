package com.gym.system.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.rabbitmq-enabled", havingValue = "true")
public class RabbitSyncPublisher implements SyncPublisher {
    private final RabbitTemplate rabbitTemplate;

    public RabbitSyncPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String actionType, String payload) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("actionType", actionType);
        msg.put("payload", payload);
        rabbitTemplate.convertAndSend("gym.sync.exchange", "gym.sync.route", msg);
    }
}
