package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ViewPaymentStatusResponse(
        @JsonProperty("payment_id") UUID paymentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        @JsonProperty("paid_at") Instant paidAt,
        @JsonProperty("expired_at") Instant expiredAt,
        @JsonProperty("payos_checkout_url") String payosCheckoutUrl,
        @JsonProperty("order_status") OrderStatus orderStatus,
        @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus
) {
}
