package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record RemoveReviewResponse(
		@JsonProperty("review_id")
		UUID reviewId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("removed_by")
		UUID removedBy,

		@JsonProperty("removed_at")
		Instant removedAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
