package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ModerateCommentResponse(
		@JsonProperty("comment_id")
		String commentId,

		String action,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("moderated_by")
		UUID moderatedBy,

		@JsonProperty("moderated_at")
		Instant moderatedAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
