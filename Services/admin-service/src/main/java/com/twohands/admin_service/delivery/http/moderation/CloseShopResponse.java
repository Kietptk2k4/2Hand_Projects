package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CloseShopResponse(
		@JsonProperty("shop_id")
		UUID shopId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("closed_by")
		UUID closedBy,

		@JsonProperty("closed_at")
		Instant closedAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
