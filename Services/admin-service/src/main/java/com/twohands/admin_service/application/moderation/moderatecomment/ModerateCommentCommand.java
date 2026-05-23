package com.twohands.admin_service.application.moderation.moderatecomment;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

public record ModerateCommentCommand(
		String commentId,
		ContentModerationAction action,
		String reason,
		String note
) {
}
