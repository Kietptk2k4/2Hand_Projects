package com.twohands.commerce_service.application.review.viewmyproductreview;

import java.util.UUID;

public record ViewMyProductReviewCommand(
        UUID buyerId,
        UUID productId
) {
}
