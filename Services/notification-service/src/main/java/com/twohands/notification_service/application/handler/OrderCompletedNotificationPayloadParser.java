package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.OrderCompletedNotificationContext;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredTimestampPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderCompletedNotificationPayloadParser {

    private static final String ORDER_AGGREGATE_TYPE = "ORDER";

    private final ObjectMapper objectMapper;

    public OrderCompletedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderCompletedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for ORDER_COMPLETED notification event.");
        }

        String orderId = firstNonBlank(
                textField(payload, "order_id"),
                ORDER_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("order_id is required for ORDER_COMPLETED notification event.");
        }

        String orderCode = firstNonBlank(
                textField(payload, "order_code"),
                orderId
        );

        String completedAt = ShipmentDeliveredTimestampPolicy.sanitize(firstNonBlank(
                textField(payload, "completed_at"),
                textField(payload, "order_completed_at")
        ));

        return new OrderCompletedNotificationContext(
                buyerId,
                orderId.trim(),
                orderCode.trim(),
                completedAt
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
            throw new IllegalArgumentException("ORDER_COMPLETED event payload must be valid JSON.");
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
