package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.util.List;
import java.util.Optional;

public interface ViewAdminReviewsForModerationRepository {

    long count(Optional<ReviewStatus> status, Optional<Integer> rating, Optional<String> searchQuery);

    List<AdminReviewListEntry> find(
            Optional<ReviewStatus> status,
            Optional<Integer> rating,
            Optional<String> searchQuery,
            PageQuery pageQuery
    );
}
