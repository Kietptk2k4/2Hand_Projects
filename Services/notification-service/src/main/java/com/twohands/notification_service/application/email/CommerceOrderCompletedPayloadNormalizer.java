package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredTimestampPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommerceOrderCompletedPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "ORDER_COMPLETED",
            "COMMERCE_ORDER_COMPLETED"
    );

    private static final Set<String> STRIP_FIELDS = Set.of(
            "internal_note",
            "settlement_raw",
            "provider_secret"
    );

    private final ObjectMapper objectMapper;

    public CommerceOrderCompletedPayloadNormalizer(ObjectMapper objectMapper) {
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

            if (!hasText(normalized, "order_code")) {
                copyTextField(normalized, "order_id", "order_code");
            }

            String completedAt = ShipmentDeliveredTimestampPolicy.sanitize(firstNonBlank(
                    textValue(normalized, "completed_at"),
                    textValue(normalized, "order_completed_at")
            ));
            normalized.remove("completed_at");
            normalized.remove("order_completed_at");
            if (completedAt != null) {
                normalized.put("completed_at", completedAt);
            }

            normalizeReviewPromptMetadata(normalized);

            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private static void normalizeReviewPromptMetadata(ObjectNode payload) {
        copyBooleanFlag(payload, "show_review_prompt", "prompt_review");
        payload.remove("reviewable_items");

        JsonNode reviewableItems = payload.get("reviewable_item_ids");
        if (reviewableItems != null && reviewableItems.isArray() && !reviewableItems.isEmpty()) {
            payload.put("has_reviewable_items", true);
            payload.put("reviewable_item_count", reviewableItems.size());
            payload.remove("reviewable_item_ids");
        }
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

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
