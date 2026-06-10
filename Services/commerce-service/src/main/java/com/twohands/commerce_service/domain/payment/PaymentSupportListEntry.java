package com.twohands.commerce_service.domain.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSupportListEntry(
        UUID paymentId,
        UUID orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant paidAt,
        Instant createdAt
) {
}
