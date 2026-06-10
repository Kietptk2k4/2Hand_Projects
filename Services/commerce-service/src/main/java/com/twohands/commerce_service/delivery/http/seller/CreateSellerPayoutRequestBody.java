package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSellerPayoutRequestBody(
        @JsonProperty("payout_account_id")
        @NotNull(message = "payout_account_id is required")
        UUID payoutAccountId,

        @JsonProperty("amount")
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be positive")
        BigDecimal amount
) {
}
