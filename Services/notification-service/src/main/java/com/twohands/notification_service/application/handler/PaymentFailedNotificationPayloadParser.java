package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.PaymentFailedNotificationContext;
import com.twohands.notification_service.domain.commerce.PaymentFailedReasonPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentFailedNotificationPayloadParser {

    private static final String PAYMENT_AGGREGATE_TYPE = "PAYMENT";
    private static final String ORDER_AGGREGATE_TYPE = "ORDER";
    private static final String REFERENCE_TYPE_PAYMENT = "PAYMENT";
    private static final String REFERENCE_TYPE_ORDER = "ORDER";

    private final ObjectMapper objectMapper;

    public PaymentFailedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PaymentFailedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for PAYMENT_FAILED notification event.");
        }

        String paymentId = firstNonBlank(
                textField(payload, "payment_id"),
                PAYMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        String orderId = firstNonBlank(
                textField(payload, "order_id"),
                ORDER_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        if ((paymentId == null || paymentId.isBlank()) && (orderId == null || orderId.isBlank())) {
            throw new IllegalArgumentException(
                    "payment_id or order_id is required for PAYMENT_FAILED notification event."
            );
        }

        String orderCode = firstNonBlank(
                textField(payload, "order_code"),
                orderId
        );

        String referenceType;
        String referenceId;
        if (paymentId != null && !paymentId.isBlank()) {
            referenceType = REFERENCE_TYPE_PAYMENT;
            referenceId = paymentId.trim();
        } else {
            referenceType = REFERENCE_TYPE_ORDER;
            referenceId = orderId.trim();
        }

        String userFacingFailureReason = firstNonBlank(
                textField(payload, "user_failure_reason"),
                PaymentFailedReasonPolicy.resolveUserFacingReason(
                        firstNonBlank(
                                textField(payload, "failure_reason"),
                                textField(payload, "reason"),
                                textField(payload, "failure_message")
                        ),
                        textField(payload, "reason_code")
                )
        );

        return new PaymentFailedNotificationContext(
                buyerId,
                paymentId == null ? null : paymentId.trim(),
                orderId == null ? null : orderId.trim(),
                orderCode == null ? null : orderCode.trim(),
                referenceType,
                referenceId,
                userFacingFailureReason
        );
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("PAYMENT_FAILED event payload must be valid JSON.");
        }
    }

    private UUID firstUuid(UUID primary, String firstFallback, String secondFallback) {
        if (primary != null) {
            return primary;
        }
        UUID first = parseUuid(firstFallback);
        if (first != null) {
            return first;
        }
        return parseUuid(secondFallback);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
