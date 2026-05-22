package com.twohands.commerce_service.domain.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ViewPaymentStatusSnapshot(
        UUID paymentId,
        UUID orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant paidAt,
        Instant expiredAt,
        String payosCheckoutUrl,
        Instant checkoutUrlExpiredAt,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus
) {
}
