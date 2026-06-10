package com.twohands.commerce_service.application.finance.viewsellerrevenuetrend;

import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record ViewSellerRevenueTrendCommand(
        UUID sellerId,
        Optional<Instant> from,
        Optional<Instant> toExclusive,
        RevenueTrendGranularity granularity
) {
}
