package com.twohands.admin_service.application.moderation.hidereview;

import java.util.UUID;

public record HideReviewCommand(
		UUID reviewId,
		String reason,
		String note
) {
}
