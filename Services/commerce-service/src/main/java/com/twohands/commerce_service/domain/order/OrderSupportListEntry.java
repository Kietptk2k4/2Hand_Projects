package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSupportListEntry(
        UUID orderId,
        UUID buyerId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal finalAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
