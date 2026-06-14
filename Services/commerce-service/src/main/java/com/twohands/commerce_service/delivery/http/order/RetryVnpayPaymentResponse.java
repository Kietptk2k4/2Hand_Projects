package com.twohands.commerce_service.delivery.http.order;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RetryVnpayPaymentResponse(
        @JsonProperty("order_id")
        UUID orderId,
        @JsonProperty("payment_id")
        UUID paymentId,
        @JsonProperty("txn_ref")
        String txnRef,
        String redirect
) {
}
