package com.twohands.admin_service.application.moderation.removeproduct;

import java.time.Instant;
import java.util.UUID;

public record RemoveProductResult(
		UUID productId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID removedBy,
		Instant removedAt,
		UUID outboxEventId
) {
}
