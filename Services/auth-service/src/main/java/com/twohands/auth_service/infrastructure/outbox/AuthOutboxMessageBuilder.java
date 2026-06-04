package com.twohands.auth_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuthOutboxMessageBuilder {

    private static final String SOURCE = "auth";

    private final ObjectMapper objectMapper;
    private final AuthOutboxEventKeyResolver eventKeyResolver;

    public AuthOutboxMessageBuilder(ObjectMapper objectMapper, AuthOutboxEventKeyResolver eventKeyResolver) {
        this.objectMapper = objectMapper;
        this.eventKeyResolver = eventKeyResolver;
    }

    public Map<String, Object> buildEnvelope(OutboxEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("event_id", event.id().toString());
        envelope.put("event_type", event.eventType());
        envelope.put("event_key", eventKeyResolver.resolve(event));
        envelope.put("source", SOURCE);
        envelope.put("occurred_at", event.createdAt().toString());
        envelope.put("payload", parsePayload(event.payload()));
        return envelope;
    }

    public String buildEnvelopeJson(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(buildEnvelope(event));
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize auth outbox envelope: " + ex.getMessage());
        }
    }

    private Object parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode node = objectMapper.readTree(payload);
            if (node.isObject() || node.isArray()) {
                return objectMapper.convertValue(node, Object.class);
            }
            return node.asText();
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Invalid auth outbox payload JSON: " + ex.getMessage());
        }
    }
}
