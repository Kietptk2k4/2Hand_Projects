package com.twohands.admin_service.application.moderation.removereview;

import java.util.UUID;

public record RemoveReviewCommand(
		UUID reviewId,
		String reason,
		String note
) {
}
