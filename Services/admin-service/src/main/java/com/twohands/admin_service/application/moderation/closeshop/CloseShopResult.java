package com.twohands.admin_service.application.moderation.closeshop;

import java.time.Instant;
import java.util.UUID;

public record CloseShopResult(
		UUID shopId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID closedBy,
		Instant closedAt,
		UUID outboxEventId
) {
}
