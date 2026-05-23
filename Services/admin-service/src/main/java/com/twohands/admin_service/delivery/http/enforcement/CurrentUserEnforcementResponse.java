package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CurrentUserEnforcementResponse(
		@JsonProperty("enforcement_id")
		UUID enforcementId,
		@JsonProperty("user_id")
		UUID userId,
		@JsonProperty("action_type")
		String actionType,
		@JsonProperty("reason_code")
		String reasonCode,
		String description,
		@JsonProperty("expires_at")
		Instant expiresAt,
		@JsonProperty("enforced_by")
		UUID enforcedBy,
		@JsonProperty("created_at")
		Instant createdAt,
		@JsonProperty("possibly_expired")
		boolean possiblyExpired
) {
}
