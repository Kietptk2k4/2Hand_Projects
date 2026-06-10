package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;

public record SellerRevenueSummary(
        SellerRevenueBucket inTransit,
        SellerRevenueBucket pendingConfirm,
        SellerRevenueBucket recognized,
        BigDecimal totalGross,
        SellerBalanceSummary balance,
        Instant from,
        Instant to
) {
}
