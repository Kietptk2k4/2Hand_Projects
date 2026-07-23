package com.twohands.admin_service.application.moderation.viewcommenthistory;

import java.util.List;

public record ViewCommentModerationHistoryResult(
		String commentId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<CommentModerationHistoryItem> history
) {
}
