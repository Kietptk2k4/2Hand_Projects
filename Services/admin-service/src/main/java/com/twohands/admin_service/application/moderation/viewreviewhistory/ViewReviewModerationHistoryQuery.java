package com.twohands.admin_service.application.moderation.viewreviewhistory;

import java.util.UUID;

public record ViewReviewModerationHistoryQuery(
		UUID reviewId,
		Integer page,
		Integer size
) {
}
