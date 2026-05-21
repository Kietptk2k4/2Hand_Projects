package com.twohands.commerce_service.application.order.createorder;

import com.twohands.commerce_service.domain.order.CreateOrderLineRequest;
import com.twohands.commerce_service.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID buyerId,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        PaymentMethod paymentMethod,
        String idempotencyKey,
        List<CreateOrderLineRequest> lines,
        Instant occurredAt
) {
}
