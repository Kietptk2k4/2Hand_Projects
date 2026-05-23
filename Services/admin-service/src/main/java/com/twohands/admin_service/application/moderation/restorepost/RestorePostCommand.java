package com.twohands.admin_service.application.moderation.restorepost;

public record RestorePostCommand(
		String postId,
		String reason,
		String note
) {
}
