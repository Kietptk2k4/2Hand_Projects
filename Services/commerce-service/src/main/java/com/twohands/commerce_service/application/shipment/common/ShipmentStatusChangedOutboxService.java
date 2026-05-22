package com.twohands.commerce_service.application.shipment.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShipmentStatusChangedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHIPMENT_STATUS_CHANGED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShipmentStatusChangedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID shipmentId,
            UUID orderId,
            UUID sellerId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            Instant occurredAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipment_id", shipmentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("old_status", oldStatus.name());
        payload.put("new_status", newStatus.name());
        payload.put("changed_at", occurredAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shipment:" + shipmentId + ":status:" + newStatus.name(),
                shipmentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                occurredAt,
                null,
                null
        );
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }
    }
}
