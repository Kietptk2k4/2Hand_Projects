package com.twohands.commerce_service.application.payment.createpayment;

import com.twohands.commerce_service.domain.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePaymentCommand(
        UUID paymentId,
        UUID orderId,
        UUID payerId,
        UUID buyerId,
        BigDecimal amount,
        BigDecimal orderFinalAmount,
        PaymentMethod paymentMethod,
        PaymentMethod orderPaymentMethod,
        String currency,
        String idempotencyKey,
        Instant occurredAt
) {
}
