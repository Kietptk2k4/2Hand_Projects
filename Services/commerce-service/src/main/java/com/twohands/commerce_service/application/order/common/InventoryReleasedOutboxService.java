package com.twohands.commerce_service.application.order.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
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
public class InventoryReleasedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_INVENTORY_RELEASED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public InventoryReleasedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID orderId, List<OrderItemQuantity> releasedItems, Instant releasedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId.toString());
        payload.put("released_at", releasedAt.toString());
        payload.put(
                "items",
                releasedItems.stream()
                        .map(item -> Map.of(
                                "product_id", item.productId().toString(),
                                "quantity", item.quantity()
                        ))
                        .toList()
        );

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "inventory:" + orderId + ":released",
                orderId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                releasedAt,
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
