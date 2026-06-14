package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredTimestampPolicy;
import com.twohands.notification_service.domain.commerce.ShipmentTrackingCodePolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommerceShipmentNotificationPayloadNormalizer {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "SHIPMENT_CREATED",
            "COMMERCE_SHIPMENT_CREATED",
            "SHIPMENT_READY_TO_SHIP",
            "COMMERCE_SHIPMENT_READY_TO_SHIP",
            "SHIPMENT_CANCELLED",
            "COMMERCE_SHIPMENT_CANCELLED",
            "SHIPMENT_SHIPPED",
            "COMMERCE_SHIPMENT_SHIPPED",
            "SHIPMENT_DELIVERED",
            "COMMERCE_SHIPMENT_DELIVERED"
    );

    private static final Set<String> STRIP_FIELDS = Set.of(
            "provider_secret",
            "raw_webhook",
            "webhook_payload",
            "carrier_raw_response",
            "internal_note"
    );

    private final ObjectMapper objectMapper;

    public CommerceShipmentNotificationPayloadNormalizer(ObjectMapper objectMapper) {
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

            String trackingCode = ShipmentTrackingCodePolicy.sanitize(textValue(normalized, "tracking_code"));
            normalized.remove("tracking_code");
            if (trackingCode != null) {
                normalized.put("tracking_code", trackingCode);
            }

            String deliveredAt = ShipmentDeliveredTimestampPolicy.sanitize(firstNonBlank(
                    textValue(normalized, "delivered_at"),
                    textValue(normalized, "delivery_at")
            ));
            normalized.remove("delivered_at");
            normalized.remove("delivery_at");
            if (deliveredAt != null) {
                normalized.put("delivered_at", deliveredAt);
            }

            normalizeReceiptPromptFlags(normalized);

            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private static void normalizeReceiptPromptFlags(ObjectNode payload) {
        copyBooleanFlag(payload, "prompt_confirm_receipt", "show_confirm_receipt");
        copyBooleanFlag(payload, "show_review_prompt", "prompt_review");
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

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
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
