package com.twohands.commerce_service.domain.finance;

import java.time.Instant;
import java.util.UUID;

public record SellerPayoutAccount(
        UUID id,
        UUID sellerId,
        String bankName,
        String bankAccountName,
        String bankAccountNumber,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
