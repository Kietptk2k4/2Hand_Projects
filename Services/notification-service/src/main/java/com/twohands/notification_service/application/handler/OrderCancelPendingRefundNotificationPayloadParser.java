package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.OrderCancelPendingRefundNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderCancelPendingRefundNotificationPayloadParser {

    private final ObjectMapper objectMapper;

    public OrderCancelPendingRefundNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderCancelPendingRefundNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for ORDER_CANCEL_PENDING_REFUND notification event.");
        }

        String orderId = firstNonBlank(
                textField(payload, "order_id"),
                "ORDER".equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("order_id is required for ORDER_CANCEL_PENDING_REFUND notification event.");
        }

        return new OrderCancelPendingRefundNotificationContext(
                buyerId,
                orderId.trim(),
                textField(payload, "refund_request_id"),
                CommerceNotificationPayloadSupport.parseSellerIds(payload, textField(payload, "seller_id")),
                textField(payload, "reason"),
                textField(payload, "requested_by"),
                parseUuid(textField(payload, "requested_by_user_id"))
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
            throw new IllegalArgumentException("ORDER_CANCEL_PENDING_REFUND event payload must be valid JSON.");
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

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
