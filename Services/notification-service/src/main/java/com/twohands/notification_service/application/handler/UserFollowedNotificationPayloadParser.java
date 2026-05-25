package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.UserFollowedNotificationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserFollowedNotificationPayloadParser {

    private final ObjectMapper objectMapper;

    public UserFollowedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserFollowedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID actorId = firstUuid(
                event.actorId(),
                textField(payload, "actor_id")
        );
        UUID followedUserId = firstUuid(
                event.recipientUserId(),
                textField(payload, "followed_user_id")
        );

        if (followedUserId == null) {
            throw new IllegalArgumentException("followed_user_id is required for USER_FOLLOWED notification event.");
        }

        return new UserFollowedNotificationContext(actorId, followedUserId);
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("USER_FOLLOWED event payload must be valid JSON.");
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
}
