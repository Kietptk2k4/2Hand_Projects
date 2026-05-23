package com.twohands.admin_service.delivery.http.investigation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record InvestigationEnforcementSummaryResponse(
		@JsonProperty("enforcement_id")
		UUID enforcementId,
		@JsonProperty("action_type")
		String actionType,
		@JsonProperty("reason_code")
		String reasonCode,
		String status,
		@JsonProperty("expires_at")
		Instant expiresAt,
		@JsonProperty("possibly_expired")
		boolean possiblyExpired
) {
}
