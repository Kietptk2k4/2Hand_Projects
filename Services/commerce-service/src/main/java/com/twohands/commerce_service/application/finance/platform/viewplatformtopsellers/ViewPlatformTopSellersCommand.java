package com.twohands.commerce_service.application.finance.platform.viewplatformtopsellers;

import java.time.Instant;
import java.util.Optional;

public record ViewPlatformTopSellersCommand(Optional<Instant> from, Optional<Instant> toExclusive, Integer limit) {
}
