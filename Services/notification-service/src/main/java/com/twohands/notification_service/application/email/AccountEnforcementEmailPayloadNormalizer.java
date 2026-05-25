package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.email.AccountEnforcementEmailReasonPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AccountEnforcementEmailPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "USER_SUSPENDED",
            "USER_RESTRICTED"
    );

    private static final Set<String> INTERNAL_FIELDS = Set.of(
            "enforced_by",
            "revoked_by",
            "note",
            "revoke_reason",
            "admin_note",
            "internal_note"
    );

    private final ObjectMapper objectMapper;

    public AccountEnforcementEmailPayloadNormalizer(ObjectMapper objectMapper) {
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
            normalizeRecipientFields(normalized);
            normalizeEnforcementContext(normalized);
            INTERNAL_FIELDS.forEach(normalized::remove);
            normalized.remove("description");
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private void normalizeRecipientFields(ObjectNode payload) {
        copyTextField(payload, "user_id", "target_user_id");
        if (!hasText(payload, "recipient_email")) {
            copyTextField(payload, "email", "recipient_email");
        }
    }

    private void normalizeEnforcementContext(ObjectNode payload) {
        String userFacingReason = AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                firstNonBlank(
                        textValue(payload, "enforcement_reason"),
                        textValue(payload, "description"),
                        textValue(payload, "user_reason"),
                        textValue(payload, "reason")
                ),
                textValue(payload, "reason_code")
        );
        if (userFacingReason != null && !userFacingReason.isBlank()) {
            payload.put("enforcement_reason", userFacingReason);
        }

        String expiresAt = firstNonBlank(
                textValue(payload, "enforcement_expires_at"),
                textValue(payload, "expires_at")
        );
        if (expiresAt != null) {
            payload.put("enforcement_expires_at", expiresAt);
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
