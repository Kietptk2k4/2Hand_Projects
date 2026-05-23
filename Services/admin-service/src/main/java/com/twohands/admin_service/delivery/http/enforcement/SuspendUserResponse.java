package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record SuspendUserResponse(
		@JsonProperty("enforcement_id")
		UUID enforcementId,
		@JsonProperty("user_id")
		UUID userId,
		@JsonProperty("reason_code")
		String reasonCode,
		String status,
		@JsonProperty("expires_at")
		Instant expiresAt,
		@JsonProperty("enforced_by")
		UUID enforcedBy,
		@JsonProperty("created_at")
		Instant createdAt,
		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
