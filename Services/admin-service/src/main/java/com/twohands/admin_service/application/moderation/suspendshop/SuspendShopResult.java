package com.twohands.admin_service.application.moderation.suspendshop;

import java.time.Instant;
import java.util.UUID;

public record SuspendShopResult(
		UUID shopId,
		UUID moderationLogId,
		String reason,
		String note,
		UUID suspendedBy,
		Instant suspendedAt,
		UUID outboxEventId
) {
}
