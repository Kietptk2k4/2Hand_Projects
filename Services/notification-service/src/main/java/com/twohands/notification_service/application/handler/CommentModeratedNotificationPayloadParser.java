package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.CommentModeratedNotificationContext;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommentModeratedNotificationPayloadParser {

    private static final String COMMENT_AGGREGATE_TYPE = "COMMENT";
    private static final String REFERENCE_TYPE = "COMMENT";

    private final ObjectMapper objectMapper;

    public CommentModeratedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CommentModeratedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload(), event.eventType());

        UUID authorUserId = firstUuid(
                event.recipientUserId(),
                textField(payload, "author_user_id")
        );
        if (authorUserId == null) {
            throw new IllegalArgumentException(
                    "author_user_id is required for " + event.eventType() + " notification event."
            );
        }

        String commentId = firstNonBlank(
                textField(payload, "comment_id"),
                COMMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (commentId == null || commentId.isBlank()) {
            throw new IllegalArgumentException(
                    "comment_id is required for " + event.eventType() + " notification event."
            );
        }

        String action = firstNonBlank(textField(payload, "action"));
        String moderationReason = firstNonBlank(
                textField(payload, "moderation_reason"),
                textField(payload, "reason")
        );

        return new CommentModeratedNotificationContext(
                authorUserId,
                commentId.trim(),
                firstNonBlank(textField(payload, "post_id")),
                action,
                moderationReason,
                REFERENCE_TYPE,
                commentId.trim(),
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

    private JsonNode parsePayload(String rawPayload, String eventType) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException(eventType + " event payload must be valid JSON.");
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
