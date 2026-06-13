package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReviewContextSnapshot(
        UUID orderItemId,
        UUID orderId,
        UUID productId,
        String status,
        String productNameSnapshot,
        String imageSnapshot,
        String shopNameSnapshot,
        BigDecimal finalPrice,
        Instant completedAt,
        boolean hasReview,
        UUID reviewId
) {
}
