package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;

public record PlatformRevenueTrendPoint(
        Instant periodStart,
        BigDecimal gmvAmount,
        BigDecimal platformFeeAmount,
        long itemCount
) {
}
