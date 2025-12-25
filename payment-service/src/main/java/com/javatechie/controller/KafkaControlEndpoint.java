package com.javatechie.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@Profile("test")
@RequestMapping("/kafka")
public class KafkaControlEndpoint {

    private final KafkaListenerController controller;

    public KafkaControlEndpoint(KafkaListenerController controller) {
        this.controller = controller;
    }

    @PostMapping("/pause/{id}")
    public void pause(@PathVariable String id) {
        controller.pause(id);
    }

    @PostMapping("/resume/{id}")
    public void resume(@PathVariable String id) {
        controller.resume(id);
    }
}