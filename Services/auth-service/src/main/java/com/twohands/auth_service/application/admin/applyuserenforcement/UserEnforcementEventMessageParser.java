package com.twohands.auth_service.application.admin.applyuserenforcement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class UserEnforcementEventMessageParser {

    private final ObjectMapper objectMapper;

    public UserEnforcementEventMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConsumeUserEnforcementEventCommand parse(String rawMessage, String topic, String fallbackEventType) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidUserEnforcementEventException("Event message body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            JsonNode payload = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            String eventType = text(root, "event_type");
            if (eventType == null) {
                eventType = fallbackEventType;
            }
            if (eventType == null || eventType.isBlank()) {
                throw new InvalidUserEnforcementEventException("event_type is required in message or topic mapping");
            }

            UUID eventId = uuid(root, "event_id");
            if (eventId == null) {
                eventId = uuid(payload, "event_id");
            }
            if (eventId == null) {
                throw new InvalidUserEnforcementEventException("event_id is required in message");
            }

            UUID userId = uuid(payload, "user_id");
            if (userId == null) {
                userId = uuid(root, "user_id");
            }
            if (userId == null) {
                throw new InvalidUserEnforcementEventException("user_id is required in payload");
            }

            UUID enforcementId = uuid(payload, "enforcement_id");
            if (enforcementId == null) {
                throw new InvalidUserEnforcementEventException("enforcement_id is required in payload");
            }

            Instant occurredAt = instant(root, "occurred_at");
            if (occurredAt == null) {
                occurredAt = instant(payload, "occurred_at");
            }
            if (occurredAt == null) {
                occurredAt = instant(payload, "expired_at");
            }

            return new ConsumeUserEnforcementEventCommand(
                    eventId,
                    eventType.trim(),
                    enforcementId,
                    userId,
                    text(payload, "action_type"),
                    text(payload, "reason_code"),
                    resolveDescription(payload),
                    instant(payload, "expires_at"),
                    occurredAt
            );
        } catch (InvalidUserEnforcementEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidUserEnforcementEventException(
                    "Cannot parse user enforcement event message: " + ex.getMessage()
            );
        }
    }

    private String resolveDescription(JsonNode payload) {
        String description = text(payload, "description");
        if (description != null) {
            return description;
        }
        return firstNonBlank(
                text(payload, "revoke_reason"),
                text(payload, "note")
        );
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
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
            throw new InvalidUserEnforcementEventException("Invalid UUID for field " + field);
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
            throw new InvalidUserEnforcementEventException("Invalid instant for field " + field);
        }
    }
}
