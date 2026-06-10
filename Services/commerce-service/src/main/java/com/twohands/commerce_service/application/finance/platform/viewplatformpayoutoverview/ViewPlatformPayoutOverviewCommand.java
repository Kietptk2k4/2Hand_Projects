package com.twohands.commerce_service.application.finance.platform.viewplatformpayoutoverview;

import java.time.Instant;
import java.util.Optional;

public record ViewPlatformPayoutOverviewCommand(Optional<Instant> from, Optional<Instant> toExclusive) {
}
