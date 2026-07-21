package com.twohands.admin_service.application.moderation.viewposthistory;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record PostModerationHistoryItem(
		UUID moderationLogId,
		ContentModerationAction action,
		String reason,
		String note,
		UUID adminId,
		Instant createdAt
) {
}
