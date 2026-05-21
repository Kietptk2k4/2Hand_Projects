package com.twohands.commerce_service.domain.payment;

import java.util.UUID;

public record CreatePaymentResult(
        UUID paymentId,
        PaymentStatus status
) {
}
