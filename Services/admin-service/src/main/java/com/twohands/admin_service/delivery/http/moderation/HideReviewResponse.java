package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record HideReviewResponse(
		@JsonProperty("review_id")
		UUID reviewId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("hidden_by")
		UUID hiddenBy,

		@JsonProperty("hidden_at")
		Instant hiddenAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
