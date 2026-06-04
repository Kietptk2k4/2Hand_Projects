package com.twohands.notification_service.application.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;

@Component
public class JacksonNotificationEventPayloadSanitizer implements NotificationEventPayloadSanitizer {

    private static final int MAX_JSON_LENGTH = 16_000;
    private static final int MAX_DEPTH = 6;
    private static final int MAX_FIELDS_PER_OBJECT = 50;
    private static final Set<String> NON_SECRET_KEYS = Set.of(
            "verification_code",
            "reset_code"
    );

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "token",
            "otp",
            "secret",
            "authorization",
            "cookie",
            "refresh_token",
            "access_token",
            "api_key",
            "credential",
            "bearer",
            "device_token"
    );

    private final ObjectMapper objectMapper;

    public JacksonNotificationEventPayloadSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String sanitize(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return "{}";
        }
        try {
            JsonNode node = unwrapTextualJson(objectMapper.readTree(rawPayload));
            if (!node.isObject() && !node.isArray()) {
                throw invalidPayload("payload", "Payload must be a JSON object or array.");
            }
            JsonNode sanitized = redact(node, MAX_DEPTH, 0);
            return truncate(objectMapper.writeValueAsString(sanitized));
        } catch (JsonProcessingException ex) {
            throw invalidPayload("payload", "Payload must be valid JSON.");
        }
    }

    private JsonNode redact(JsonNode node, int maxDepth, int depth) {
        if (node == null) {
            return objectMapper.nullNode();
        }
        if (depth >= maxDepth) {
            return objectMapper.getNodeFactory().textNode("[MAX_DEPTH]");
        }
        if (node.isObject()) {
            ObjectNode sanitized = objectMapper.createObjectNode();
            int fieldCount = 0;
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext() && fieldCount < MAX_FIELDS_PER_OBJECT) {
                String fieldName = fieldNames.next();
                fieldCount++;
                if (isSensitiveKey(fieldName)) {
                    sanitized.put(fieldName, "***REDACTED***");
                } else {
                    sanitized.set(fieldName, redact(node.get(fieldName), maxDepth, depth + 1));
                }
            }
            if (fieldNames.hasNext()) {
                sanitized.put("_truncated_fields", true);
            }
            return sanitized;
        }
        if (node.isArray()) {
            ArrayNode sanitized = objectMapper.createArrayNode();
            int index = 0;
            for (JsonNode child : node) {
                if (index >= MAX_FIELDS_PER_OBJECT) {
                    sanitized.add("[ARRAY_TRUNCATED]");
                    break;
                }
                sanitized.add(redact(child, maxDepth, depth + 1));
                index++;
            }
            return sanitized;
        }
        return node;
    }

    private JsonNode unwrapTextualJson(JsonNode node) {
        if (!node.isTextual()) {
            return node;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return unwrapTextualJson(objectMapper.readTree(text));
        } catch (JsonProcessingException ex) {
            throw invalidPayload("payload", "Payload must be valid JSON.");
        }
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase().replace('-', '_');
        if (NON_SECRET_KEYS.contains(normalized)) {
            return false;
        }
        return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
    }

    private String truncate(String json) {
        if (json.length() <= MAX_JSON_LENGTH) {
            return json;
        }
        return json.substring(0, MAX_JSON_LENGTH) + "...[TRUNCATED]";
    }

    private AppException invalidPayload(String field, String reason) {
        return new AppException(ErrorCode.INVALID_EVENT_PAYLOAD, "Validation failed", field, reason);
    }
}
