package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;
import java.util.UUID;

public record ViewProductReviewsResult(
        UUID productId,
        ProductReviewRatingSummary ratingSummary,
        List<ProductReviewListItem> reviews,
        PageMeta pagination
) {
}
