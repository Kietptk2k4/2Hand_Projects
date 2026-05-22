package com.twohands.commerce_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CommerceOutboxMessageBuilder {

    private final ObjectMapper objectMapper;

    public CommerceOutboxMessageBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> buildEnvelope(OutboxEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("event_id", event.id().toString());
        envelope.put("event_type", event.eventType());
        envelope.put("event_key", event.eventKey());
        envelope.put("aggregate_id", event.aggregateId().toString());
        envelope.put("source", event.source());
        envelope.put("occurred_at", event.createdAt().toString());
        envelope.put("payload", parsePayload(event.payload()));
        return envelope;
    }

    public String buildEnvelopeJson(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(buildEnvelope(event));
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox envelope", ex);
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
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Invalid outbox payload JSON", ex);
        }
    }
}
