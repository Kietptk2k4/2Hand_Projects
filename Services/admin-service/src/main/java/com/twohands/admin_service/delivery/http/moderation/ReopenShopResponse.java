package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ReopenShopResponse(
		@JsonProperty("shop_id")
		UUID shopId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("reopened_by")
		UUID reopenedBy,

		@JsonProperty("reopened_at")
		Instant reopenedAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
