package com.twohands.commerce_service.domain.payment;

import java.util.UUID;

public record LockedPaymentContext(
        UUID paymentId,
        UUID orderId,
        String paymentStatus,
        String paymentMethod,
        String orderStatus,
        String orderPaymentStatus
) {
    public boolean isPaid() {
        return PaymentStatus.PAID.name().equals(paymentStatus);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.name().equals(paymentStatus);
    }

    public boolean isCod() {
        return PaymentMethod.COD.name().equals(paymentMethod);
    }
}
