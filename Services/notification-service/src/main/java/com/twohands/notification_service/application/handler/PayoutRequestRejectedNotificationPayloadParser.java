package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.PayoutRequestRejectedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PayoutRequestRejectedNotificationPayloadParser {

    private static final String PAYOUT_REQUEST_AGGREGATE_TYPE = "PAYOUT_REQUEST";

    private final ObjectMapper objectMapper;

    public PayoutRequestRejectedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PayoutRequestRejectedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID sellerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "seller_id")
        );
        if (sellerId == null) {
            throw new IllegalArgumentException("seller_id is required for PAYOUT_REQUEST_REJECTED notification event.");
        }

        String payoutRequestId = firstNonBlank(
                textField(payload, "payout_request_id"),
                PAYOUT_REQUEST_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (payoutRequestId == null || payoutRequestId.isBlank()) {
            throw new IllegalArgumentException("payout_request_id is required for PAYOUT_REQUEST_REJECTED notification event.");
        }

        String adminNote = textField(payload, "admin_note");
        if (adminNote == null || adminNote.isBlank()) {
            throw new IllegalArgumentException("admin_note is required for PAYOUT_REQUEST_REJECTED notification event.");
        }

        return new PayoutRequestRejectedNotificationContext(
                sellerId,
                payoutRequestId.trim(),
                parseAmount(payload.get("amount")),
                adminNote.trim(),
                textField(payload, "rejected_at")
        );
    }

    private BigDecimal parseAmount(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("PAYOUT_REQUEST_REJECTED event payload must be valid JSON.");
        }
    }

    private UUID firstUuid(UUID primary, String fallback) {
        if (primary != null) {
            return primary;
        }
        return parseUuid(fallback);
    }

    private UUID parseUuid(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String textField(JsonNode payload, String field) {
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

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }
}
