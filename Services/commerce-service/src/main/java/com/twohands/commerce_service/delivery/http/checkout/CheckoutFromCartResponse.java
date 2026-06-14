package com.twohands.commerce_service.delivery.http.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutFromCartResponse(
        @JsonProperty("order_id")
        UUID orderId,
        @JsonProperty("payment_id")
        UUID paymentId,
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,
        @JsonProperty("payment_status")
        PaymentStatus paymentStatus,
        @JsonProperty("order_status")
        OrderStatus orderStatus,
        @JsonProperty("final_amount")
        BigDecimal finalAmount,
        @JsonProperty("payos_checkout_url")
        String payosCheckoutUrl,
        String redirect
) {
}
