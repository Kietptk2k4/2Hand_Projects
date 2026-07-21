package com.twohands.admin_service.application.moderation.viewposthistory;

import java.util.List;

public record ViewPostModerationHistoryResult(
		String postId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<PostModerationHistoryItem> history
) {
}
