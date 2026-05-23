package com.twohands.admin_service.application.moderation.moderatepost;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record ModeratePostResult(
		String postId,
		ContentModerationAction action,
		UUID moderationLogId,
		String reason,
		String note,
		UUID moderatedBy,
		Instant moderatedAt,
		UUID outboxEventId
) {
}
