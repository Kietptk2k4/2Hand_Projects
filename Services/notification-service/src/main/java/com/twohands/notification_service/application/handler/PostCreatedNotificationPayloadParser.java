package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.PostCreatedNotificationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PostCreatedNotificationPayloadParser {

    private static final String POST_AGGREGATE_TYPE = "POST";

    private final ObjectMapper objectMapper;

    public PostCreatedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PostCreatedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID actorId = firstUuid(
                event.actorId(),
                textField(payload, "actor_id"),
                textField(payload, "user_id")
        );
        if (actorId == null) {
            throw new IllegalArgumentException("actor_id is required for POST_CREATED notification event.");
        }

        UUID postAuthorId = firstUuid(
                null,
                textField(payload, "post_author_id"),
                actorId.toString()
        );
        if (postAuthorId == null) {
            throw new IllegalArgumentException("post_author_id is required for POST_CREATED notification event.");
        }

        String postId = firstNonBlank(
                textField(payload, "post_id"),
                POST_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (postId == null || postId.isBlank()) {
            throw new IllegalArgumentException("post_id is required for POST_CREATED notification event.");
        }

        List<UUID> followerUserIds = SocialFanOutFollowerIdsPayloadSupport.parseFollowerUserIds(payload);
        if (followerUserIds.isEmpty()) {
            throw new IllegalArgumentException("follower_user_ids is required for POST_CREATED notification event.");
        }

        return new PostCreatedNotificationContext(actorId, postAuthorId, postId.trim(), followerUserIds);
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("POST_CREATED event payload must be valid JSON.");
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

    private UUID firstUuid(UUID primary, String... fallbacks) {
        if (primary != null) {
            return primary;
        }
        for (String fallback : fallbacks) {
            UUID parsed = parseUuid(fallback);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
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
