package com.twohands.admin_service.application.moderation.removeproduct;

import java.util.UUID;

public record RemoveProductCommand(
		UUID productId,
		String reason,
		String note
) {
}
