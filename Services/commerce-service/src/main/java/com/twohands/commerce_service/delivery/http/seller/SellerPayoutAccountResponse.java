package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerPayoutAccount;

import java.time.Instant;
import java.util.UUID;

public record SellerPayoutAccountResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("bank_account_name") String bankAccountName,
        @JsonProperty("bank_account_number") String bankAccountNumber,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
    public static SellerPayoutAccountResponse from(SellerPayoutAccount account) {
        return new SellerPayoutAccountResponse(
                account.id(),
                account.bankName(),
                account.bankAccountName(),
                account.bankAccountNumber(),
                account.isDefault(),
                account.createdAt(),
                account.updatedAt()
        );
    }
}
