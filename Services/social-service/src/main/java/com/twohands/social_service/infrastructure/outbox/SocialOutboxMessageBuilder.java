package com.twohands.social_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SocialOutboxMessageBuilder {

    private static final String SOURCE = "social";

    private final ObjectMapper objectMapper;
    private final SocialOutboxEventKeyResolver eventKeyResolver;

    public SocialOutboxMessageBuilder(ObjectMapper objectMapper, SocialOutboxEventKeyResolver eventKeyResolver) {
        this.objectMapper = objectMapper;
        this.eventKeyResolver = eventKeyResolver;
    }

    public Map<String, Object> buildEnvelope(OutboxEvent event) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("event_id", event.id().toString());
        envelope.put("event_type", event.eventType());
        envelope.put("event_key", eventKeyResolver.resolve(event));
        envelope.put("aggregate_id", event.aggregateId());
        envelope.put("source", SOURCE);
        envelope.put("occurred_at", event.createdAt().toString());
        Object payload = parsePayload(event.payload());
        envelope.put("payload", payload);
        applyEnvelopeRouting(envelope, payload);
        return envelope;
    }

    public String buildEnvelopeJson(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(buildEnvelope(event));
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize social outbox envelope: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void applyEnvelopeRouting(Map<String, Object> envelope, Object payload) {
        if (!(payload instanceof Map<?, ?> payloadMap)) {
            return;
        }
        Map<String, Object> map = (Map<String, Object>) payloadMap;

        String actorId = firstNonBlank(
                text(map.get("actor_id")),
                text(map.get("user_id")),
                text(map.get("follower_id"))
        );
        if (actorId != null) {
            envelope.put("actor_id", actorId);
        }

        String recipientId = firstNonBlank(
                text(map.get("post_author_id")),
                text(map.get("followed_user_id")),
                text(map.get("followee_id"))
        );
        if (recipientId != null) {
            envelope.put("recipient_user_ids", List.of(recipientId));
            return;
        }

        Object followerIds = map.get("follower_user_ids");
        if (followerIds instanceof List<?> list && !list.isEmpty()) {
            envelope.put("recipient_user_ids", list);
        }
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Invalid social outbox payload JSON: " + ex.getMessage());
        }
    }
}
