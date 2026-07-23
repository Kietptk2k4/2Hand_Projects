package com.twohands.admin_service.application.moderation.viewcommenthistory;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record CommentModerationHistoryItem(
		UUID moderationLogId,
		ContentModerationAction action,
		String reason,
		String note,
		UUID adminId,
		Instant createdAt
) {
}
