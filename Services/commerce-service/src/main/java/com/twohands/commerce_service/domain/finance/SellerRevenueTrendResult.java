package com.twohands.commerce_service.domain.finance;

import java.time.Instant;
import java.util.List;

public record SellerRevenueTrendResult(
        RevenueTrendGranularity granularity,
        Instant from,
        Instant to,
        List<SellerRevenueTrendPoint> points
) {
}
