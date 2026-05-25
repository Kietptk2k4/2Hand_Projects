package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.email.AccountEnforcementEmailReasonPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminReviewModerationPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of("REVIEW_HIDDEN");

    private static final Set<String> STRIP_FIELDS = Set.of(
            "hidden_by",
            "removed_by",
            "restored_by",
            "moderation_log_id",
            "admin_note",
            "internal_note",
            "note"
    );

    private final ObjectMapper objectMapper;

    public AdminReviewModerationPayloadNormalizer(ObjectMapper objectMapper) {
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
            copyTextField(normalized, "author_id", "review_author_id");
            copyTextField(normalized, "author_user_id", "review_author_id");
            copyTextField(normalized, "seller_id", "seller_user_id");

            String userFacingReason = AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                    firstNonBlank(
                            textValue(normalized, "hidden_reason"),
                            textValue(normalized, "reason"),
                            textValue(normalized, "description")
                    ),
                    textValue(normalized, "reason_code")
            );
            STRIP_FIELDS.forEach(normalized::remove);
            normalized.remove("reason");
            normalized.remove("description");
            if (userFacingReason != null && !userFacingReason.isBlank()) {
                normalized.put("hidden_reason", userFacingReason);
            }

            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
