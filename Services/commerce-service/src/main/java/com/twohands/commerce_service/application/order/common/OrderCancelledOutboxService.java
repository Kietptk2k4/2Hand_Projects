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
import java.util.Map;
import java.util.UUID;

@Component
public class OrderCancelledOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_ORDER_CANCELLED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public OrderCancelledOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID orderId, String reason, Instant cancelledAt) {
        return build(orderId, reason, "SYSTEM", cancelledAt);
    }

    public OutboxEvent build(UUID orderId, String reason, String cancelledBy, Instant cancelledAt) {
        return build(orderId, null, reason, cancelledBy, cancelledAt);
    }

    public OutboxEvent build(UUID orderId, UUID buyerId, String reason, String cancelledBy, Instant cancelledAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId.toString());
        if (buyerId != null) {
            payload.put("buyer_id", buyerId.toString());
        }
        payload.put("reason", reason);
        payload.put("cancelled_at", cancelledAt.toString());
        payload.put("cancelled_by", cancelledBy);

        return buildEvent(orderId, payload, cancelledAt);
    }

    private OutboxEvent buildEvent(UUID orderId, Map<String, Object> payload, Instant cancelledAt) {
        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "order:" + orderId + ":cancelled",
                orderId,
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
