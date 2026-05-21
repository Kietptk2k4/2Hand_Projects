package com.twohands.commerce_service.delivery.http.checkout;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutFromCartResponse(
        UUID orderId,
        UUID paymentId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        BigDecimal finalAmount,
        String payosCheckoutUrl
) {
}
