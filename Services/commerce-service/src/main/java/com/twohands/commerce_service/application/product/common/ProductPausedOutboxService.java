package com.twohands.commerce_service.application.product.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductPausedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PRODUCT_PAUSED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ProductPausedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID productId,
            UUID shopId,
            UUID sellerId,
            ProductStatus previousStatus,
            Instant pausedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("product_id", productId.toString());
        payload.put("shop_id", shopId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("previous_status", previousStatus.name());
        payload.put("paused_at", pausedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "product:" + productId + ":paused",
                productId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                pausedAt,
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
