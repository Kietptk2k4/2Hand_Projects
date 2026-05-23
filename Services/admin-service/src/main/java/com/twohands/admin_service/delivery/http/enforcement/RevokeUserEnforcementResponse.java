package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record RevokeUserEnforcementResponse(
		@JsonProperty("enforcement_id")
		UUID enforcementId,
		@JsonProperty("user_id")
		UUID userId,
		@JsonProperty("action_type")
		String actionType,
		String status,
		@JsonProperty("revoked_by")
		UUID revokedBy,
		@JsonProperty("updated_at")
		Instant updatedAt,
		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
