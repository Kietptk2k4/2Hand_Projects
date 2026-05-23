package com.twohands.admin_service.delivery.http.investigation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewUserProfileForInvestigationResponse(
		@JsonProperty("user_id")
		UUID userId,
		String email,
		String status,
		@JsonProperty("email_verified")
		boolean emailVerified,
		@JsonProperty("phone_verified")
		boolean phoneVerified,
		@JsonProperty("last_login_at")
		Instant lastLoginAt,
		@JsonProperty("created_at")
		Instant createdAt,
		@JsonProperty("display_name")
		String displayName,
		@JsonProperty("avatar_url")
		String avatarUrl,
		String bio,
		String website,
		@JsonProperty("is_private")
		boolean isPrivate,
		@JsonProperty("current_enforcements")
		List<InvestigationEnforcementSummaryResponse> currentEnforcements
) {
}
