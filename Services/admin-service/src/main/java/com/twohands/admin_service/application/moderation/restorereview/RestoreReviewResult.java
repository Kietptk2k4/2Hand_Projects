package com.twohands.admin_service.application.moderation.restorereview;

import java.time.Instant;
import java.util.UUID;

public record RestoreReviewResult(
		UUID reviewId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID restoredBy,
		Instant restoredAt,
		UUID outboxEventId
) {
}
