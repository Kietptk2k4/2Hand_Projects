package com.twohands.admin_service.application.moderation.restorecomment;

public record RestoreCommentCommand(
		String commentId,
		String reason,
		String note
) {
}
