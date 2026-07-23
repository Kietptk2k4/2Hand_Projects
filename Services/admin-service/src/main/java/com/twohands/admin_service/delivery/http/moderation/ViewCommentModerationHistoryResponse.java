package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ViewCommentModerationHistoryResponse(
		@JsonProperty("comment_id")
		String commentId,

		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		List<CommentModerationHistoryEntryResponse> history
) {
}
