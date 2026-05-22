package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record UpdateProductReviewDraft(
        UUID reviewId,
        UUID buyerId,
        int rating,
        String comment
) {
}
