package com.twohands.commerce_service.domain.finance;

import java.time.Instant;
import java.util.UUID;

public record SellerLedgerCreditDraft(
        UUID sellerId,
        UUID orderItemId,
        SellerLedgerAmounts amounts,
        Instant createdAt
) {
}
