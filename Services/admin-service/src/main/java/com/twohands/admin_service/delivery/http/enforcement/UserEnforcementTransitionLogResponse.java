package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserEnforcementTransitionLogResponse(
		@JsonProperty("log_id")
		UUID logId,
		@JsonProperty("old_status")
		String oldStatus,
		@JsonProperty("new_status")
		String newStatus,
		@JsonProperty("admin_id")
		UUID adminId,
		@JsonProperty("actor_type")
		String actorType,
		String note,
		@JsonProperty("created_at")
		Instant createdAt
) {
}
