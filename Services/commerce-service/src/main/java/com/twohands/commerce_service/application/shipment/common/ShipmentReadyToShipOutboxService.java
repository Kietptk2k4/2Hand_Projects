package com.twohands.commerce_service.application.shipment.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShipmentReadyToShipOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHIPMENT_READY_TO_SHIP";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShipmentReadyToShipOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID shipmentId,
            UUID orderId,
            UUID buyerId,
            UUID sellerId,
            String trackingCode,
            Instant readyAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipment_id", shipmentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("ready_at", readyAt.toString());
        if (trackingCode != null && !trackingCode.isBlank()) {
            payload.put("tracking_code", trackingCode);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shipment:" + shipmentId + ":ready_to_ship",
                shipmentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                readyAt,
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
