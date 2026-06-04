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
public class PaymentPaidOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYMENT_PAID";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PaymentPaidOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID paymentId,
            UUID orderId,
            UUID buyerId,
            String reason,
            Instant paidAt,
            String orderCode
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payment_id", paymentId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("reason", reason);
        payload.put("paid_at", paidAt.toString());

        String resolvedOrderCode = orderCode != null && !orderCode.isBlank()
                ? orderCode.trim()
                : orderId.toString();
        payload.put("order_code", resolvedOrderCode);

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payment:" + paymentId + ":paid",
                paymentId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                paidAt,
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
