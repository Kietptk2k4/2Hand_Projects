package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.PostModeratedNotificationContext;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PostModeratedNotificationPayloadParser {

    private static final String POST_AGGREGATE_TYPE = "POST";
    private static final String REFERENCE_TYPE = "POST";

    private final ObjectMapper objectMapper;

    public PostModeratedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PostModeratedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID authorUserId = firstUuid(
                event.recipientUserId(),
                textField(payload, "author_user_id"),
                textField(payload, "post_author_id")
        );
        if (authorUserId == null) {
            throw new IllegalArgumentException("author_user_id is required for POST_MODERATED notification event.");
        }

        String postId = firstNonBlank(
                textField(payload, "post_id"),
                POST_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (postId == null || postId.isBlank()) {
            throw new IllegalArgumentException("post_id is required for POST_MODERATED notification event.");
        }

        String action = firstNonBlank(textField(payload, "action"));
        String moderationReason = firstNonBlank(
                textField(payload, "moderation_reason"),
                textField(payload, "reason")
        );

        return new PostModeratedNotificationContext(
                authorUserId,
                postId.trim(),
                action,
                moderationReason,
                REFERENCE_TYPE,
                postId.trim(),
                resolveTemplateVariant(action)
        );
    }

    private String resolveTemplateVariant(String action) {
        if (action == null) {
            return null;
        }
        return switch (action.trim().toUpperCase()) {
            case "HIDE" -> InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT;
            case "REMOVE" -> InAppNotificationTemplatePolicy.REMOVE_TEMPLATE_VARIANT;
            default -> null;
        };
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("POST_MODERATED event payload must be valid JSON.");
        }
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

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String textField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
