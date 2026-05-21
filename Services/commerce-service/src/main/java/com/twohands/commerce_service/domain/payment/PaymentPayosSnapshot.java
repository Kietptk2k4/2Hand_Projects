package com.twohands.commerce_service.domain.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentPayosSnapshot(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        String payosOrderCode,
        String payosCheckoutUrl,
        Instant checkoutUrlExpiredAt,
        Instant paymentExpiredAt
) {
}
