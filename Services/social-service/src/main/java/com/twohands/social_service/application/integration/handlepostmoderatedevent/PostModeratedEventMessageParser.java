package com.twohands.social_service.application.integration.handlepostmoderatedevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.post.PostModerationAction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PostModeratedEventMessageParser {

    private final ObjectMapper objectMapper;

    public PostModeratedEventMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HandlePostModeratedEventCommand parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidPostModeratedEventException("Event message body is empty");
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
                throw new InvalidPostModeratedEventException("event_id is required in message");
            }

            String postId = text(payload, "post_id");
            if (postId == null) {
                throw new InvalidPostModeratedEventException("post_id is required in payload");
            }

            UUID moderationLogId = uuid(payload, "moderation_log_id");
            PostModerationAction action = PostModerationAction.fromValue(text(payload, "action"));
            if (action == null) {
                throw new InvalidPostModeratedEventException("action must be HIDE, REMOVE, or RESTORE");
            }

            Instant moderatedAt = instant(payload, "moderated_at");
            if (moderatedAt == null) {
                moderatedAt = instant(payload, "restored_at");
            }
            if (moderatedAt == null) {
                moderatedAt = instant(root, "occurred_at");
            }

            return new HandlePostModeratedEventCommand(
                    eventId,
                    postId,
                    moderationLogId,
                    action,
                    text(payload, "reason"),
                    uuid(payload, "moderated_by"),
                    moderatedAt
            );
        } catch (InvalidPostModeratedEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidPostModeratedEventException("Cannot parse post moderated event message: " + ex.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value;
    }

    private UUID uuid(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidPostModeratedEventException("Invalid UUID for field " + field);
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
            throw new InvalidPostModeratedEventException("Invalid instant for field " + field);
        }
    }
}
