package com.twohands.admin_service.application.moderation.restoreproduct;

import java.util.UUID;

public record RestoreProductCommand(
		UUID productId,
		String reason,
		String note
) {
}
