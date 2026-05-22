package com.twohands.commerce_service.application.review.viewproductreviews;

import java.util.UUID;

public record ViewProductReviewsCommand(
        UUID productId,
        Integer page,
        Integer limit,
        Integer rating,
        String sort
) {
}
