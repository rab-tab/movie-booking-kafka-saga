package com.javatechie.controller;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerController {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaListenerController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    public void pause(String listenerId) {
        registry.getListenerContainer(listenerId).pause();
    }

    public void resume(String listenerId) {
        registry.getListenerContainer(listenerId).resume();
    }

    public boolean isPaused(String listenerId) {
        return registry.getListenerContainer(listenerId).isPauseRequested();
    }
}