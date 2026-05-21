package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CreatePayosCheckoutUrlResponse(
        @JsonProperty("payment_id") UUID paymentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("payos_order_code") String payosOrderCode,
        @JsonProperty("payos_checkout_url") String payosCheckoutUrl,
        @JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt
) {
}
