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
public class PaymentExpiredOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYMENT_EXPIRED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PaymentExpiredOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID paymentId, UUID orderId, Instant expiredAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payment_id", paymentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("expired_at", expiredAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payment:" + paymentId + ":expired",
                paymentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                expiredAt,
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
