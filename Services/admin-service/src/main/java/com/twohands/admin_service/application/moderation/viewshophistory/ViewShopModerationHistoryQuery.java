package com.twohands.admin_service.application.moderation.viewshophistory;

import java.util.UUID;

public record ViewShopModerationHistoryQuery(
		UUID shopId,
		Integer page,
		Integer size
) {
}
