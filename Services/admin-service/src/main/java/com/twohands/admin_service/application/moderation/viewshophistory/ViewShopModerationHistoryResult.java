package com.twohands.admin_service.application.moderation.viewshophistory;

import java.util.List;
import java.util.UUID;

public record ViewShopModerationHistoryResult(
		UUID shopId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<ShopModerationHistoryItem> history
) {
}
