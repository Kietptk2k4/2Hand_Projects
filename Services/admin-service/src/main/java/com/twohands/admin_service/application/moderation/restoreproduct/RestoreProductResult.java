package com.twohands.admin_service.application.moderation.restoreproduct;

import java.time.Instant;
import java.util.UUID;

public record RestoreProductResult(
		UUID productId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID restoredBy,
		Instant restoredAt,
		UUID outboxEventId
) {
}
