package com.twohands.commerce_service.domain.admin;

import java.util.Optional;
import java.util.UUID;

public interface ViewReviewDetailForModerationRepository {

    Optional<AdminReviewDetailEntry> findByReviewId(UUID reviewId);
}
