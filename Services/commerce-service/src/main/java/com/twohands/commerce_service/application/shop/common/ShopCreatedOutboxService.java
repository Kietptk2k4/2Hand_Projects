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
public class ShopCreatedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_SHOP_CREATED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ShopCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID shopId, UUID sellerId, ShopStatus status, Instant createdAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shop_id", shopId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("status", status.name());
        payload.put("created_at", createdAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "shop:" + shopId + ":created",
                shopId,
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
