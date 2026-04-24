package com.twohands.authservice.infrastructure.message.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "system.events", groupId = "auth-group")
    public void consume(String message) {
        log.debug("Received event from system.events: {}", message);
    }
}
