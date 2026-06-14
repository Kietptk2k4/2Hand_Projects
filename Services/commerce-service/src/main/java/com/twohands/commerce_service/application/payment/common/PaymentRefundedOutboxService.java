package com.twohands.commerce_service.application.payment.common;

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
public class PaymentRefundedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYMENT_REFUNDED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PaymentRefundedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID refundRequestId,
            UUID paymentId,
            UUID orderId,
            UUID buyerId,
            BigDecimal amount,
            String adminNote,
            Instant refundedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("refund_request_id", refundRequestId.toString());
        payload.put("payment_id", paymentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("amount", amount);
        payload.put("admin_note", adminNote);
        payload.put("refunded_at", refundedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payment:" + paymentId + ":refunded",
                paymentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                refundedAt,
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
