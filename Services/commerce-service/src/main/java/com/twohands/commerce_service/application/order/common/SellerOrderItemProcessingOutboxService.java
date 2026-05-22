package com.twohands.commerce_service.application.order.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SellerOrderItemProcessingOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SELLER_ORDER_ITEM_PROCESSING";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public SellerOrderItemProcessingOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID orderId,
            UUID sellerId,
            List<UUID> orderItemIds,
            Instant occurredAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("order_item_ids", orderItemIds.stream().map(UUID::toString).toList());
        payload.put("processed_at", occurredAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "order:" + orderId + ":seller-items-processing:" + sellerId,
                orderId,
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
