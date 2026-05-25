package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommerceOrderNotificationPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "ORDER_CREATED",
            "COMMERCE_ORDER_CREATED",
            "PAYMENT_SUCCESS",
            "COMMERCE_PAYMENT_PAID"
    );

    private static final Set<String> PAYMENT_SECRET_FIELDS = Set.of(
            "provider_secret",
            "raw_webhook",
            "webhook_payload",
            "client_secret",
            "card_number",
            "card_token",
            "cvv",
            "stripe_payment_intent_secret"
    );

    private final ObjectMapper objectMapper;

    public CommerceOrderNotificationPayloadNormalizer(ObjectMapper objectMapper) {
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
            copyTextField(normalized, "email", "recipient_email");
            copyTextField(normalized, "buyer_email", "recipient_email");
            if (!hasText(normalized, "order_code")) {
                copyTextField(normalized, "order_id", "order_code");
            }
            if ("ORDER_CREATED".equals(eventType) || "COMMERCE_ORDER_CREATED".equals(eventType)) {
                normalized.remove("payment_method");
            }
            if ("PAYMENT_SUCCESS".equals(eventType) || "COMMERCE_PAYMENT_PAID".equals(eventType)) {
                PAYMENT_SECRET_FIELDS.forEach(normalized::remove);
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
}
