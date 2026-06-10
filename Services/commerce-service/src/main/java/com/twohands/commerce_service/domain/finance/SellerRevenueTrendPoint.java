package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;

public record SellerRevenueTrendPoint(
        Instant periodStart,
        BigDecimal recognizedAmount,
        long itemCount
) {
}
