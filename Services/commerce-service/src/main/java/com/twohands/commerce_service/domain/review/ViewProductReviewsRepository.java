package com.twohands.commerce_service.domain.review;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ViewProductReviewsRepository {

    Optional<ViewProductReviewsResult> findVisibleProductReviews(
            UUID productId,
            Integer ratingFilter,
            ProductReviewSort sort,
            PageQuery pageQuery,
            Instant now
    );
}
