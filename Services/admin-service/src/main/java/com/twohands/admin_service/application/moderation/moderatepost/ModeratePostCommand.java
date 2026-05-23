package com.twohands.admin_service.application.moderation.moderatepost;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;

public record ModeratePostCommand(
		String postId,
		ContentModerationAction action,
		String reason,
		String note
) {
}
