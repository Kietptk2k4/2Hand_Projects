package com.twohands.commerce_service.application.finance.platform.viewplatformsummary;

import java.time.Instant;
import java.util.Optional;

public record ViewPlatformFinanceSummaryCommand(Optional<Instant> from, Optional<Instant> toExclusive) {
}
