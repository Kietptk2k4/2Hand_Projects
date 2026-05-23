package com.twohands.admin_service.application.moderation.viewproducthistory;

import java.util.UUID;

public record ViewProductModerationHistoryQuery(
		UUID productId,
		Integer page,
		Integer size
) {
}
