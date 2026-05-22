package com.twohands.commerce_service.application.shop.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShopClosedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHOP_CLOSED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShopClosedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID shopId,
            UUID sellerId,
            UUID adminId,
            ShopStatus oldStatus,
            ShopStatus newStatus,
            String reason,
            Instant occurredAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shop_id", shopId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("admin_id", adminId.toString());
        payload.put("old_status", oldStatus.name());
        payload.put("new_status", newStatus.name());
        payload.put("reason", reason);
        payload.put("occurred_at", occurredAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shop:" + shopId + ":closed",
                shopId,
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
