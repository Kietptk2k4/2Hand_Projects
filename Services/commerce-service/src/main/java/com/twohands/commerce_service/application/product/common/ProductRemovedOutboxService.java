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
public class ProductRemovedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PRODUCT_REMOVED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ProductRemovedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID productId,
            UUID sellerId,
            UUID shopId,
            UUID adminId,
            ProductStatus previousStatus,
            String reason,
            Instant occurredAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("product_id", productId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("shop_id", shopId.toString());
        payload.put("admin_id", adminId.toString());
        payload.put("previous_status", previousStatus.name());
        payload.put("new_status", ProductStatus.REMOVED.name());
        payload.put("reason", reason);
        payload.put("occurred_at", occurredAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "product:" + productId + ":removed",
                productId,
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
