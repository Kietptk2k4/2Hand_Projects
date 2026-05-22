package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record SellerOrderListPaymentSummaryResponse(
        @JsonProperty("payment_id") UUID paymentId,
        PaymentStatus status,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency
) {
}
