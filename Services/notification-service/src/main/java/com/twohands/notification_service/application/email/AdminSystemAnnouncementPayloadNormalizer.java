package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminSystemAnnouncementPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "SYSTEM_ANNOUNCEMENT_SENT",
            "SYSTEM_ANNOUNCEMENT_PUBLISHED"
    );

    private static final Set<String> STRIP_FIELDS = Set.of(
            "created_by",
            "status"
    );

    private final ObjectMapper objectMapper;

    public AdminSystemAnnouncementPayloadNormalizer(ObjectMapper objectMapper) {
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
            copyTextField(normalized, "id", "announcement_id");
            copyBooleanField(normalized, "pinned", "is_pinned");
            STRIP_FIELDS.forEach(normalized::remove);
            normalized.remove("pinned");
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private static void copyBooleanField(ObjectNode payload, String sourceField, String targetField) {
        if (payload.has(targetField) && payload.get(targetField).isBoolean()) {
            return;
        }
        JsonNode source = payload.get(sourceField);
        if (source == null || source.isNull()) {
            return;
        }
        if (source.isBoolean()) {
            payload.put(targetField, source.asBoolean());
            return;
        }
        if (source.isValueNode()) {
            String value = source.asText();
            if (value != null && !value.isBlank()) {
                payload.put(targetField, Boolean.parseBoolean(value.trim()));
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
