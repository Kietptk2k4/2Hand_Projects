package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.util.UUID;

public interface ViewShopReviewsRepository {

    ViewShopReviewsResult findShopReviews(
            UUID sellerId,
            UUID shopId,
            Integer ratingFilter,
            ReviewStatus statusFilter,
            ProductReviewSort sort,
            PageQuery pageQuery
    );
}
