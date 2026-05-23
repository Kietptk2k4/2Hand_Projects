package com.twohands.admin_service.application.moderation.moderatecomment;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record ModerateCommentResult(
		String commentId,
		ContentModerationAction action,
		UUID moderationLogId,
		String reason,
		String note,
		UUID moderatedBy,
		Instant moderatedAt,
		UUID outboxEventId
) {
}
