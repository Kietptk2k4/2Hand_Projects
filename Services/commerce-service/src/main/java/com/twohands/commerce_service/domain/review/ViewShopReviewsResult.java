package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;
import java.util.UUID;

public record ViewShopReviewsResult(
        UUID shopId,
        ProductReviewRatingSummary ratingSummary,
        List<ShopReviewListItem> reviews,
        PageMeta pagination
) {
}
