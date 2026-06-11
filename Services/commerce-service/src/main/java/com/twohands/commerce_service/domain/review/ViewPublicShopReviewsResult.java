package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;
import java.util.UUID;

public record ViewPublicShopReviewsResult(
        UUID shopId,
        String shopName,
        ProductReviewRatingSummary ratingSummary,
        List<PublicShopReviewListItem> reviews,
        PageMeta pagination
) {
}
