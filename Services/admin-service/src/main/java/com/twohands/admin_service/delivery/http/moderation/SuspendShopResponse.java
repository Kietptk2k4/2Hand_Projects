package com.twohands.admin_service.delivery.http.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record SuspendShopResponse(
		@JsonProperty("shop_id")
		UUID shopId,

		@JsonProperty("moderation_log_id")
		UUID moderationLogId,

		String reason,

		String note,

		@JsonProperty("suspended_by")
		UUID suspendedBy,

		@JsonProperty("suspended_at")
		Instant suspendedAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
