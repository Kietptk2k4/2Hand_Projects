package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminReviewListEntry(
        UUID reviewId,
        UUID orderItemId,
        UUID productId,
        String productTitle,
        String productThumbnailUrl,
        UUID buyerId,
        UUID sellerId,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt
) {
}
