package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerLedgerListEntry(
        UUID id,
        UUID orderItemId,
        SellerLedgerEntryType entryType,
        BigDecimal grossAmount,
        BigDecimal platformFeeAmount,
        BigDecimal netAmount,
        BigDecimal commissionRateSnapshot,
        SellerLedgerEntryStatus status,
        Instant createdAt
) {
}
