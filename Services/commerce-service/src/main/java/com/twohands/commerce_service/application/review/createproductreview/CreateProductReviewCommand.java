package com.twohands.commerce_service.application.review.createproductreview;

import java.util.UUID;

public record CreateProductReviewCommand(
        UUID buyerId,
        UUID orderItemId,
        Integer rating,
        String comment
) {
}
