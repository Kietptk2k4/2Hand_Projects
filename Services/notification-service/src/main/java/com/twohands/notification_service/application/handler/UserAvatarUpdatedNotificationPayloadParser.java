package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.UserAvatarUpdatedNotificationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserAvatarUpdatedNotificationPayloadParser {

    private final ObjectMapper objectMapper;

    public UserAvatarUpdatedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserAvatarUpdatedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID actorId = firstUuid(
                event.actorId(),
                textField(payload, "actor_id"),
                textField(payload, "user_id")
        );
        if (actorId == null) {
            throw new IllegalArgumentException("actor_id is required for USER_AVATAR_UPDATED notification event.");
        }

        String avatarUrl = textField(payload, "avatar_url");
        if (avatarUrl == null) {
            throw new IllegalArgumentException("avatar_url is required for USER_AVATAR_UPDATED notification event.");
        }

        List<UUID> followerUserIds = SocialFanOutFollowerIdsPayloadSupport.parseFollowerUserIds(payload);
        if (followerUserIds.isEmpty()) {
            throw new IllegalArgumentException("follower_user_ids is required for USER_AVATAR_UPDATED notification event.");
        }

        return new UserAvatarUpdatedNotificationContext(
                actorId,
                avatarUrl,
                textField(payload, "display_name"),
                followerUserIds
        );
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("USER_AVATAR_UPDATED event payload must be valid JSON.");
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
}
