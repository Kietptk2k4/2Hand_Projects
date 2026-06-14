package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CreateVnpayCheckoutUrlResponse(
        @JsonProperty("payment_id")
        UUID paymentId,
        @JsonProperty("order_id")
        UUID orderId,
        @JsonProperty("txn_ref")
        String txnRef,
        @JsonProperty("redirect")
        String redirect
) {
}
