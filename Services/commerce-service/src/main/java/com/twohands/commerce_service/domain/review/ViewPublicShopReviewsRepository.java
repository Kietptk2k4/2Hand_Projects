package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.util.Optional;
import java.util.UUID;

public interface ViewPublicShopReviewsRepository {

    Optional<ViewPublicShopReviewsResult> findVisibleShopReviews(
            UUID shopId,
            Integer ratingFilter,
            ProductReviewSort sort,
            PageQuery pageQuery
    );
}
