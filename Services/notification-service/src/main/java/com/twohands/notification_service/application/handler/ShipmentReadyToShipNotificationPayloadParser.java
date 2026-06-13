package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.commerce.ShipmentReadyToShipNotificationContext;
import com.twohands.notification_service.domain.commerce.ShipmentTrackingCodePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShipmentReadyToShipNotificationPayloadParser {

    private static final String SHIPMENT_AGGREGATE_TYPE = "SHIPMENT";

    private final ObjectMapper objectMapper;

    public ShipmentReadyToShipNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ShipmentReadyToShipNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID buyerId = firstUuid(
                event.recipientUserId(),
                textField(payload, "buyer_id"),
                textField(payload, "buyer_user_id")
        );
        if (buyerId == null) {
            throw new IllegalArgumentException("buyer_id is required for SHIPMENT_READY_TO_SHIP notification event.");
        }

        String shipmentId = firstNonBlank(
                textField(payload, "shipment_id"),
                SHIPMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (shipmentId == null || shipmentId.isBlank()) {
            throw new IllegalArgumentException("shipment_id is required for SHIPMENT_READY_TO_SHIP notification event.");
        }

        String orderId = textField(payload, "order_id");
        String trackingCode = ShipmentTrackingCodePolicy.sanitize(textField(payload, "tracking_code"));

        return new ShipmentReadyToShipNotificationContext(
                buyerId,
                shipmentId.trim(),
                orderId,
                trackingCode
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
            throw new IllegalArgumentException("SHIPMENT_READY_TO_SHIP event payload must be valid JSON.");
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
