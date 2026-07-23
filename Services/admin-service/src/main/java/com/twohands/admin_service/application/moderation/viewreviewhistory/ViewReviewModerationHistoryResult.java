package com.twohands.admin_service.application.moderation.viewreviewhistory;

import java.util.List;
import java.util.UUID;

public record ViewReviewModerationHistoryResult(
		UUID reviewId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<ReviewModerationHistoryItem> history
) {
}
