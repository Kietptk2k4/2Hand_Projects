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
public class ShipmentCancelledOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHIPMENT_CANCELLED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShipmentCancelledOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID shipmentId,
            UUID orderId,
            UUID buyerId,
            UUID sellerId,
            String trackingCode,
            Instant cancelledAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipment_id", shipmentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("cancelled_at", cancelledAt.toString());
        if (trackingCode != null && !trackingCode.isBlank()) {
            payload.put("tracking_code", trackingCode);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shipment:" + shipmentId + ":cancelled",
                shipmentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                cancelledAt,
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
