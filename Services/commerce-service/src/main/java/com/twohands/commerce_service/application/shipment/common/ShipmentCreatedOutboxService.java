package com.twohands.commerce_service.application.shipment.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ShipmentCreatedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHIPMENT_CREATED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShipmentCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID shipmentId,
            UUID orderId,
            UUID buyerId,
            UUID sellerId,
            ShipmentCarrier carrier,
            List<UUID> orderItemIds,
            String trackingCode,
            Instant createdAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipment_id", shipmentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("carrier", carrier.name());
        payload.put("order_item_ids", orderItemIds.stream().map(UUID::toString).toList());
        payload.put("created_at", createdAt.toString());
        if (StringUtils.hasText(trackingCode)) {
            payload.put("tracking_code", trackingCode.trim());
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shipment:" + shipmentId + ":created",
                shipmentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                createdAt,
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
