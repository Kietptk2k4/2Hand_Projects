package com.twohands.admin_service.application.moderation.removereview;

import java.time.Instant;
import java.util.UUID;

public record RemoveReviewResult(
		UUID reviewId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID removedBy,
		Instant removedAt,
		UUID outboxEventId
) {
}
