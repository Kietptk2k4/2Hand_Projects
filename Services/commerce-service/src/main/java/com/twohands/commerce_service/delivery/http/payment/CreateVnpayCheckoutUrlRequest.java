package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateVnpayCheckoutUrlRequest(
        @JsonProperty("frontend_return_url")
        String frontendReturnUrl,
        @JsonProperty("vnpay_return_url")
        String vnpayReturnUrl
) {
}
