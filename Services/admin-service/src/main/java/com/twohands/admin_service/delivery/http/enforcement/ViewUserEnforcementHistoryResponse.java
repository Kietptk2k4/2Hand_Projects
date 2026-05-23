package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewUserEnforcementHistoryResponse(
		@JsonProperty("user_id")
		UUID userId,
		int page,
		int size,
		@JsonProperty("total_elements")
		long totalElements,
		@JsonProperty("total_pages")
		int totalPages,
		List<UserEnforcementHistoryItemResponse> enforcements
) {
}
