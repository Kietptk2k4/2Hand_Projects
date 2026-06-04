package com.twohands.auth_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AuthOutboxEventKeyResolver {

    private final ObjectMapper objectMapper;

    public AuthOutboxEventKeyResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String resolve(OutboxEvent event) {
        String userId = extractUserId(event.payload());
        if (userId != null && !userId.isBlank()) {
            String normalizedType = event.eventType().trim().toLowerCase().replace('_', '.');
            return "auth." + normalizedType + ":" + userId;
        }
        return event.id().toString();
    }

    private String extractUserId(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode userId = root.get("user_id");
            if (userId != null && !userId.isNull()) {
                return userId.asText();
            }
            return null;
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Invalid auth outbox payload JSON: " + ex.getMessage());
        }
    }
}
