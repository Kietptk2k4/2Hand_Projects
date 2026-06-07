package com.twohands.social_service.application.integration.consumeauthuserevents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuthUserEventMessageParser {

    private final ObjectMapper objectMapper;

    public AuthUserEventMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConsumeAuthUserEventCommand parse(String rawMessage, String topic, String fallbackEventType) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidAuthUserEventException("Event message body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            JsonNode payload = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            String eventTypeRaw = text(root, "event_type");
            if (eventTypeRaw == null) {
                eventTypeRaw = fallbackEventType;
            }
            AuthUserEventType eventType = AuthUserEventType.fromValue(eventTypeRaw);

            UUID eventId = uuid(root, "event_id");
            if (eventId == null) {
                eventId = uuid(root, "id");
            }
            if (eventId == null) {
                eventId = uuid(payload, "event_id");
            }
            if (eventId == null) {
                throw new InvalidAuthUserEventException("event_id is required in message");
            }

            UUID userId = uuid(payload, "user_id");
            if (userId == null) {
                userId = uuid(root, "user_id");
            }

            Instant occurredAt = instant(payload, "occurred_at");
            if (occurredAt == null) {
                occurredAt = instant(root, "occurred_at");
            }
            if (occurredAt == null) {
                occurredAt = instant(payload, "updated_at");
            }
            if (occurredAt == null) {
                occurredAt = instant(payload, "deleted_at");
            }

            return new ConsumeAuthUserEventCommand(
                    eventId,
                    eventType,
                    userId,
                    text(payload, "status"),
                    text(payload, "display_name"),
                    text(payload, "email"),
                    text(payload, "avatar_url"),
                    bool(payload, "is_private"),
                    text(payload, "action_type"),
                    occurredAt
            );
        } catch (InvalidAuthUserEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidAuthUserEventException("Cannot parse auth user event message: " + ex.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value;
    }

    private Boolean bool(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isTextual()) {
            return Boolean.parseBoolean(value.asText());
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
            throw new InvalidAuthUserEventException("Invalid UUID for field " + field);
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
            throw new InvalidAuthUserEventException("Invalid instant for field " + field);
        }
    }
}
