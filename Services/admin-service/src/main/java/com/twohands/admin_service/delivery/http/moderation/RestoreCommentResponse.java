package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record RestoreCommentResponse(
		@JsonProperty("comment_id")
		String commentId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("restored_by")
		UUID restoredBy,

		@JsonProperty("restored_at")
		Instant restoredAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
