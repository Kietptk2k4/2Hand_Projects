package com.twohands.social_service.application.integration.handlecommentmoderatedevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.comment.CommentModerationAction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CommentModeratedEventMessageParser {

    private final ObjectMapper objectMapper;

    public CommentModeratedEventMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HandleCommentModeratedEventCommand parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidCommentModeratedEventException("Event message body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            JsonNode payload = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            UUID eventId = uuid(root, "event_id");
            if (eventId == null) {
                eventId = uuid(payload, "event_id");
            }
            if (eventId == null) {
                eventId = uuid(payload, "moderation_log_id");
            }
            if (eventId == null) {
                throw new InvalidCommentModeratedEventException("event_id is required in message");
            }

            String commentId = text(payload, "comment_id");
            if (commentId == null) {
                throw new InvalidCommentModeratedEventException("comment_id is required in payload");
            }

            UUID moderationLogId = uuid(payload, "moderation_log_id");
            CommentModerationAction action = CommentModerationAction.fromValue(text(payload, "action"));
            if (action == null) {
                throw new InvalidCommentModeratedEventException("action must be HIDE, REMOVE, or RESTORE");
            }

            Instant moderatedAt = instant(payload, "moderated_at");
            if (moderatedAt == null) {
                moderatedAt = instant(payload, "restored_at");
            }
            if (moderatedAt == null) {
                moderatedAt = instant(root, "occurred_at");
            }

            return new HandleCommentModeratedEventCommand(
                    eventId,
                    commentId,
                    moderationLogId,
                    action,
                    text(payload, "reason"),
                    firstUuid(payload, "moderated_by", "restored_by"),
                    moderatedAt
            );
        } catch (InvalidCommentModeratedEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidCommentModeratedEventException(
                    "Cannot parse comment moderated event message: " + ex.getMessage()
            );
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value;
    }

    private UUID firstUuid(JsonNode node, String... fields) {
        for (String field : fields) {
            UUID value = uuid(node, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private UUID uuid(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCommentModeratedEventException("Invalid UUID for field " + field);
        }
    }

    private Instant instant(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            throw new InvalidCommentModeratedEventException("Invalid instant for field " + field);
        }
    }
}
