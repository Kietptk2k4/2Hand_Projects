package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CommentModerationHistoryEntryResponse(
		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String action,
		String reason,
		String note,

		@JsonProperty("admin_id")
		UUID adminId,

		@JsonProperty("created_at")
		Instant createdAt
) {
}
