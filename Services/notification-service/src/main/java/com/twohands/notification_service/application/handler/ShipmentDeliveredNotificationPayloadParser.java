package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredNotificationContext;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredTimestampPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShipmentDeliveredNotificationPayloadParser {

    private static final String SHIPMENT_AGGREGATE_TYPE = "SHIPMENT";
    private static final String ORDER_AGGREGATE_TYPE = "ORDER";
    private static final String REFERENCE_TYPE_SHIPMENT = "SHIPMENT";
    private static final String REFERENCE_TYPE_ORDER = "ORDER";

    private final ObjectMapper objectMapper;

    public ShipmentDeliveredNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ShipmentDeliveredNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for SHIPMENT_DELIVERED notification event.");
        }

        String shipmentId = firstNonBlank(
                textField(payload, "shipment_id"),
                SHIPMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        String orderId = firstNonBlank(
                textField(payload, "order_id"),
                ORDER_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );

        if ((shipmentId == null || shipmentId.isBlank()) && (orderId == null || orderId.isBlank())) {
            throw new IllegalArgumentException(
                    "shipment_id or order_id is required for SHIPMENT_DELIVERED notification event."
            );
        }

        String referenceType;
        String referenceId;
        if (shipmentId != null && !shipmentId.isBlank()) {
            referenceType = REFERENCE_TYPE_SHIPMENT;
            referenceId = shipmentId.trim();
        } else {
            referenceType = REFERENCE_TYPE_ORDER;
            referenceId = orderId.trim();
        }

        String deliveredAt = ShipmentDeliveredTimestampPolicy.sanitize(firstNonBlank(
                textField(payload, "delivered_at"),
                textField(payload, "delivery_at")
        ));

        return new ShipmentDeliveredNotificationContext(
                buyerId,
                shipmentId == null ? null : shipmentId.trim(),
                orderId == null ? null : orderId.trim(),
                referenceType,
                referenceId,
                deliveredAt
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
            throw new IllegalArgumentException("SHIPMENT_DELIVERED event payload must be valid JSON.");
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
