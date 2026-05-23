package com.twohands.admin_service.application.moderation.closeshop;

import java.util.UUID;

public record CloseShopCommand(
		UUID shopId,
		String reason,
		String note
) {
}
