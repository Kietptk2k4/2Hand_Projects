package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewShopModerationHistoryResponse(
		@JsonProperty("shop_id")
		UUID shopId,

		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		List<ShopModerationHistoryEntryResponse> history
) {
}
