package com.twohands.commerce_service.application.review.moderatereview;

import com.twohands.commerce_service.domain.review.ReviewModerationAction;

import java.util.UUID;

public record ModerateReviewCommand(
        UUID adminId,
        UUID reviewId,
        ReviewModerationAction action,
        String reason
) {
}
