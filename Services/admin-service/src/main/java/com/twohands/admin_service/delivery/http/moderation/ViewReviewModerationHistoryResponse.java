package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewReviewModerationHistoryResponse(
		@JsonProperty("review_id")
		UUID reviewId,

		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		List<ReviewModerationHistoryEntryResponse> history
) {
}
