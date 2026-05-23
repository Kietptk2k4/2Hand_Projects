package com.twohands.admin_service.application.moderation.restorecomment;

import java.time.Instant;
import java.util.UUID;

public record RestoreCommentResult(
		String commentId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID restoredBy,
		Instant restoredAt,
		UUID outboxEventId
) {
}
