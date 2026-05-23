package com.twohands.admin_service.application.moderation.viewproducthistory;

import java.util.List;
import java.util.UUID;

public record ViewProductModerationHistoryResult(
		UUID productId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<ProductModerationHistoryItem> history
) {
}
