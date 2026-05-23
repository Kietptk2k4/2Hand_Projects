package com.twohands.admin_service.application.moderation.restorepost;

import java.time.Instant;
import java.util.UUID;

public record RestorePostResult(
		String postId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID restoredBy,
		Instant restoredAt,
		UUID outboxEventId
) {
}
