package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.PostLikedNotificationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PostLikedNotificationPayloadParser {

    private static final String POST_AGGREGATE_TYPE = "POST";

    private final ObjectMapper objectMapper;

    public PostLikedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PostLikedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID actorId = firstUuid(
                event.actorId(),
                textField(payload, "actor_id")
        );
        UUID postAuthorId = firstUuid(
                event.recipientUserId(),
                textField(payload, "post_author_id")
        );
        String postId = firstNonBlank(
                textField(payload, "post_id"),
                POST_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        if (postAuthorId == null) {
            throw new IllegalArgumentException("post_author_id is required for POST_LIKED notification event.");
        }
        if (postId == null || postId.isBlank()) {
            throw new IllegalArgumentException("post_id is required for POST_LIKED notification event.");
        }

        return new PostLikedNotificationContext(actorId, postAuthorId, postId.trim());
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("POST_LIKED event payload must be valid JSON.");
        }
    }

    private String textField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.asText();
            return value == null || value.isBlank() ? null : value.trim();
        }
        return null;
    }

    private UUID firstUuid(UUID primary, String fallbackText) {
        if (primary != null) {
            return primary;
        }
        return parseUuid(fallbackText);
    }

    private UUID parseUuid(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }
}
