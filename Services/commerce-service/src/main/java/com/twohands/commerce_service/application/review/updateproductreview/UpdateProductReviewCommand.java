package com.twohands.commerce_service.application.review.updateproductreview;

import java.util.UUID;

public record UpdateProductReviewCommand(
        UUID buyerId,
        UUID reviewId,
        Integer rating,
        String comment
) {
}
