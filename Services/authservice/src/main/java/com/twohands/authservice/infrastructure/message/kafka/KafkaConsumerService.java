package com.twohands.authservice.infrastructure.message.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "auth.events", groupId = "auth-group")
    public void consume(String message) {
        System.out.println("Received: " + message);
    }
}
