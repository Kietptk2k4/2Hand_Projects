package com.twohands.admin_service.application.moderation.restorereview;

import java.util.UUID;

public record RestoreReviewCommand(
		UUID reviewId,
		String reason,
		String note
) {
}
