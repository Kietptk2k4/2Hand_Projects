package com.twohands.notification_service.domain.notificationevent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class NotificationEventPayloadCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private NotificationEventPayloadCodec() {
    }

    public static String decode(String storedPayload) {
        if (storedPayload == null || storedPayload.isBlank()) {
            return "{}";
        }
        try {
            JsonNode node = unwrapTextualJson(OBJECT_MAPPER.readTree(storedPayload));
            if (node == null || node.isNull()) {
                return "{}";
            }
            if (node.isObject() || node.isArray()) {
                return OBJECT_MAPPER.writeValueAsString(node);
            }
        } catch (JsonProcessingException ignored) {
            // Keep original payload when it is not JSON.
        }
        return storedPayload;
    }

    private static JsonNode unwrapTextualJson(JsonNode node) throws JsonProcessingException {
        if (node == null || !node.isTextual()) {
            return node;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return OBJECT_MAPPER.createObjectNode();
        }
        return unwrapTextualJson(OBJECT_MAPPER.readTree(text));
    }
}
