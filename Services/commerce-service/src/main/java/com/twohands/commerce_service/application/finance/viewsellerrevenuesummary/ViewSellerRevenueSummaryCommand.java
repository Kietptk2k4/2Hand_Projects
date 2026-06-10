package com.twohands.commerce_service.application.finance.viewsellerrevenuesummary;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record ViewSellerRevenueSummaryCommand(
        UUID sellerId,
        Optional<Instant> from,
        Optional<Instant> toExclusive
) {
}
