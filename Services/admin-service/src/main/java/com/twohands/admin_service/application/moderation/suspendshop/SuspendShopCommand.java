package com.twohands.admin_service.application.moderation.suspendshop;

import java.util.UUID;

public record SuspendShopCommand(
		UUID shopId,
		String reason,
		String note
) {
}
