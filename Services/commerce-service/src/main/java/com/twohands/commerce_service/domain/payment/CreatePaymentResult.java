package com.twohands.commerce_service.domain.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentResult(
        UUID paymentId,
        UUID orderId,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency
) {
}
