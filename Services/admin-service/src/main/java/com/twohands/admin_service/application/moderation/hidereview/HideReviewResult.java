package com.twohands.admin_service.application.moderation.hidereview;

import java.time.Instant;
import java.util.UUID;

public record HideReviewResult(
		UUID reviewId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID hiddenBy,
		Instant hiddenAt,
		UUID outboxEventId
) {
}
