package com.twohands.admin_service.application.moderation.viewproducthistory;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record ProductModerationHistoryItem(
		UUID moderationLogId,
		ContentModerationAction action,
		String reason,
		String note,
		UUID adminId,
		Instant createdAt
) {
}
