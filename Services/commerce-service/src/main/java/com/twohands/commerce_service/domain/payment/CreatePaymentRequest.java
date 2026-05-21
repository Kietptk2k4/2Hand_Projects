package com.twohands.commerce_service.domain.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID payerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String idempotencyKey,
        Instant occurredAt
) {
}
