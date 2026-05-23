package com.twohands.admin_service.application.moderation.reopenshop;

import java.time.Instant;
import java.util.UUID;

public record ReopenShopResult(
		UUID shopId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID reopenedBy,
		Instant reopenedAt,
		UUID outboxEventId
) {
}
