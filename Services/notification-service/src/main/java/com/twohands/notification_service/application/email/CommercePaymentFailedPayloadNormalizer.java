package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.commerce.PaymentFailedReasonPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommercePaymentFailedPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "PAYMENT_FAILED",
            "COMMERCE_PAYMENT_FAILED"
    );

    private static final Set<String> STRIP_FIELDS = Set.of(
            "provider_secret",
            "raw_webhook",
            "webhook_payload",
            "client_secret",
            "card_number",
            "card_token",
            "cvv",
            "stripe_payment_intent_secret",
            "provider_error",
            "provider_raw_response",
            "error_stack",
            "stack_trace"
    );

    private final ObjectMapper objectMapper;

    public CommercePaymentFailedPayloadNormalizer(ObjectMapper objectMapper) {
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
            String userFacingReason = PaymentFailedReasonPolicy.resolveUserFacingReason(
                    firstNonBlank(
                            textValue(normalized, "failure_reason"),
                            textValue(normalized, "reason"),
                            textValue(normalized, "failure_message")
                    ),
                    textValue(normalized, "reason_code")
            );
            STRIP_FIELDS.forEach(normalized::remove);
            normalized.remove("failure_reason");
            normalized.remove("reason");
            normalized.remove("failure_message");
            if (userFacingReason != null) {
                normalized.put("user_failure_reason", userFacingReason);
            }

            if (!hasText(normalized, "order_code")) {
                copyTextField(normalized, "order_id", "order_code");
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

    private static String firstNonBlank(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        if (third != null && !third.isBlank()) {
            return third;
        }
        return null;
    }
}
