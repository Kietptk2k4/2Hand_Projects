package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommerceReviewReminderPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "REVIEW_REMINDER",
            "COMMERCE_REVIEW_REMINDER"
    );

    private static final Set<String> STRIP_FIELDS = Set.of(
            "internal_note",
            "provider_secret",
            "settlement_raw"
    );

    private final ObjectMapper objectMapper;

    public CommerceReviewReminderPayloadNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String normalizeForStorage(String eventType, String rawPayload) {
        if (!SUPPORTED_EVENT_TYPES.contains(eventType) || rawPayload == null || rawPayload.isBlank()) {
            return rawPayload;
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            if (!root.isObject()) {
                return rawPayload;
            }
            ObjectNode normalized = ((ObjectNode) root).deepCopy();
            STRIP_FIELDS.forEach(normalized::remove);

            copyTextField(normalized, "item_id", "order_item_id");
            if (!hasText(normalized, "order_code")) {
                copyTextField(normalized, "order_id", "order_code");
            }

            normalizeReviewFlags(normalized);
            ensureReminderDayPresent(normalized);

            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private static void ensureReminderDayPresent(ObjectNode payload) {
        JsonNode reminderDay = payload.get("reminder_day");
        if (reminderDay != null && !reminderDay.isNull()) {
            payload.put("reminder_day", reminderDay.asInt());
        }
    }

    private static void normalizeReviewFlags(ObjectNode payload) {
        copyBooleanFlag(payload, "already_reviewed", "review_exists", "has_review");
        payload.remove("review_exists");
        payload.remove("has_review");
    }

    private static void copyBooleanFlag(ObjectNode payload, String targetField, String... sourceFields) {
        if (payload.has(targetField)) {
            return;
        }
        for (String sourceField : sourceFields) {
            JsonNode node = payload.get(sourceField);
            if (node != null && node.isBoolean()) {
                payload.put(targetField, node.asBoolean());
                return;
            }
            if (node != null && node.isValueNode()) {
                String value = node.asText();
                if (value != null && !value.isBlank()) {
                    payload.put(targetField, Boolean.parseBoolean(value.trim()));
                    return;
                }
            }
        }
    }

    private static void copyTextField(ObjectNode payload, String sourceField, String targetField) {
        if (hasText(payload, targetField)) {
            return;
        }
        String value = textValue(payload, sourceField);
        if (value != null) {
            payload.put(targetField, value);
        }
    }

    private static boolean hasText(ObjectNode payload, String field) {
        String value = textValue(payload, field);
        return value != null && !value.isBlank();
    }

    private static String textValue(ObjectNode payload, String field) {
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
}
