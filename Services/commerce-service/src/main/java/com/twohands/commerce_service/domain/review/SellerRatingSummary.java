package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;

public record SellerRatingSummary(
        BigDecimal ratingAvg,
        int ratingCount
) {
}
