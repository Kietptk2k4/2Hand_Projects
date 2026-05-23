package com.twohands.admin_service.application.moderation.reopenshop;

import java.util.UUID;

public record ReopenShopCommand(
		UUID shopId,
		String reason,
		String note
) {
}
