package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentSupportSearchCriteria(
        PaymentStatus status,
        PaymentMethod paymentMethod,
        UUID orderId,
        Instant from,
        Instant to
) {
}
