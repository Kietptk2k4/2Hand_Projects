package com.twohands.admin_service.application.moderation.viewposthistory;

public record ViewPostModerationHistoryQuery(
		String postId,
		Integer page,
		Integer size
) {
}
