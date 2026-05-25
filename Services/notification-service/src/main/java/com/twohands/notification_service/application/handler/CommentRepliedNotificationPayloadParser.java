package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.CommentRepliedNotificationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommentRepliedNotificationPayloadParser {

    private static final String COMMENT_AGGREGATE_TYPE = "COMMENT";

    private final ObjectMapper objectMapper;

    public CommentRepliedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CommentRepliedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID actorId = firstUuid(
                event.actorId(),
                textField(payload, "actor_id")
        );
        UUID parentCommentAuthorId = firstUuid(
                event.recipientUserId(),
                textField(payload, "parent_comment_author_id")
        );
        String parentCommentId = textField(payload, "parent_comment_id");
        String commentId = firstNonBlank(
                textField(payload, "comment_id"),
                COMMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        if (parentCommentAuthorId == null) {
            throw new IllegalArgumentException(
                    "parent_comment_author_id is required for COMMENT_REPLIED notification event."
            );
        }
        if (parentCommentId == null || parentCommentId.isBlank()) {
            throw new IllegalArgumentException(
                    "parent_comment_id is required for COMMENT_REPLIED notification event."
            );
        }
        if (commentId == null || commentId.isBlank()) {
            throw new IllegalArgumentException("comment_id is required for COMMENT_REPLIED notification event.");
        }

        return new CommentRepliedNotificationContext(
                actorId,
                parentCommentAuthorId,
                parentCommentId.trim(),
                commentId.trim()
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
            throw new IllegalArgumentException("COMMENT_REPLIED event payload must be valid JSON.");
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
