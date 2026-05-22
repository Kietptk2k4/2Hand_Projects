package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;

public record ProductReviewRatingSummary(
        BigDecimal ratingAvg,
        int ratingCount
) {
}
