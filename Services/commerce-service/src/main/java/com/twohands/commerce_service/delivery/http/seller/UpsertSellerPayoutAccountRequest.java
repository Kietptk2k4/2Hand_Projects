package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertSellerPayoutAccountRequest(
        @JsonProperty("bank_name")
        @NotBlank(message = "bank_name is required")
        @Size(max = 255)
        String bankName,

        @JsonProperty("bank_account_name")
        @NotBlank(message = "bank_account_name is required")
        @Size(max = 255)
        String bankAccountName,

        @JsonProperty("bank_account_number")
        @NotBlank(message = "bank_account_number is required")
        @Size(max = 64)
        String bankAccountNumber,

        @JsonProperty("is_default")
        boolean isDefault
) {
}
