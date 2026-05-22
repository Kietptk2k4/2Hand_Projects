package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record ReviewForModeration(
        UUID reviewId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        int rating,
        ReviewStatus status
) {
}
