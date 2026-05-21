package com.twohands.commerce_service.application.order.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderCreatedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_ORDER_CREATED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public OrderCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID orderId,
            UUID buyerId,
            BigDecimal finalAmount,
            String paymentMethod,
            Instant createdAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("final_amount", finalAmount);
        payload.put("payment_method", paymentMethod);
        payload.put("created_at", createdAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "order:" + orderId + ":created",
                orderId,
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
