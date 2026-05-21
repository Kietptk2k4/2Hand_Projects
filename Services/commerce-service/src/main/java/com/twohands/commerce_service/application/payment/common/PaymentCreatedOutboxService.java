package com.twohands.commerce_service.application.payment.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentCreatedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYMENT_CREATED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PaymentCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID paymentId,
            UUID orderId,
            UUID payerId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            Instant createdAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payment_id", paymentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("payer_id", payerId.toString());
        payload.put("amount", amount);
        payload.put("currency", currency);
        payload.put("payment_method", paymentMethod.name());
        payload.put("status", status.name());
        payload.put("created_at", createdAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payment:" + paymentId + ":created",
                paymentId,
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
