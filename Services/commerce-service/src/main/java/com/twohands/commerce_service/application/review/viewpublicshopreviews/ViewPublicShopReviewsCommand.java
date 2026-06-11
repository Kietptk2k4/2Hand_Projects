package com.twohands.commerce_service.application.review.viewpublicshopreviews;

import java.util.UUID;

public record ViewPublicShopReviewsCommand(
        UUID shopId,
        Integer page,
        Integer limit,
        Integer rating,
        String sort
) {
}
