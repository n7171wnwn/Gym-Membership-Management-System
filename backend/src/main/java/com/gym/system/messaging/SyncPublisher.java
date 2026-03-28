package com.gym.system.messaging;

public interface SyncPublisher {
    void publish(String actionType, String payload);
}
