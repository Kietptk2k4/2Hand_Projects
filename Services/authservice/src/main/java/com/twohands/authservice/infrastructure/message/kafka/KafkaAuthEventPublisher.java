package com.twohands.authservice.infrastructure.message.kafka;

import com.twohands.authservice.application.auth.port.AuthEventPublisher;
import java.util.HashMap;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaAuthEventPublisher implements AuthEventPublisher {

    private static final String TOPIC = "auth.verification.requested";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaAuthEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishVerification(String email, String otp) {
        Map<String, String> event = new HashMap<>();
        event.put("email", email);
        event.put("otp", otp);
        kafkaTemplate.send(TOPIC, event);
    }
}
