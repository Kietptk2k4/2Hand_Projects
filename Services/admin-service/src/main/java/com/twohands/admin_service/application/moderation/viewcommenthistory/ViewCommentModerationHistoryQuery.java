package com.twohands.admin_service.application.moderation.viewcommenthistory;

public record ViewCommentModerationHistoryQuery(
		String commentId,
		Integer page,
		Integer size
) {
}
