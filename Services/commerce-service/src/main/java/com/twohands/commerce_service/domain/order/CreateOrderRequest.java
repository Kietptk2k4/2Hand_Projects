package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        PaymentMethod paymentMethod,
        String idempotencyKey,
        List<CreateOrderLineRequest> lines,
        Instant occurredAt
) {
}
