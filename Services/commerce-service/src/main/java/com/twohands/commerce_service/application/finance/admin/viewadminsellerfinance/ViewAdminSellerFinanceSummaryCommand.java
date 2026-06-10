package com.twohands.commerce_service.application.finance.admin.viewadminsellerfinance;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record ViewAdminSellerFinanceSummaryCommand(
        UUID sellerId,
        Optional<Instant> from,
        Optional<Instant> toExclusive
) {
}
