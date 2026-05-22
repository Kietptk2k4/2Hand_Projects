package com.twohands.commerce_service.application.review.viewshopreviews;

import java.util.UUID;

public record ViewShopReviewsCommand(
        UUID sellerId,
        Integer page,
        Integer limit,
        Integer rating,
        String status
) {
}
