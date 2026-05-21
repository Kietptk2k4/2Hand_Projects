package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record CreateProductReviewDraft(
        UUID orderItemId,
        UUID buyerId,
        UUID sellerId,
        int rating,
        String comment
) {
}
