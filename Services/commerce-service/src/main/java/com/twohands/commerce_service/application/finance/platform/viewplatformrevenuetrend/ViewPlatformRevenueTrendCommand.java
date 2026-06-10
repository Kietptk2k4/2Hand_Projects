package com.twohands.commerce_service.application.finance.platform.viewplatformrevenuetrend;

import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;

import java.time.Instant;
import java.util.Optional;

public record ViewPlatformRevenueTrendCommand(
        Optional<Instant> from,
        Optional<Instant> toExclusive,
        RevenueTrendGranularity granularity
) {
}
