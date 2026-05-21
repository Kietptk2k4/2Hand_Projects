package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        UUID paymentId,
        OrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        List<CreateOrderItemResult> orderItems
) {
}
