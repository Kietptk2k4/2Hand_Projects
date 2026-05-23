package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ViewUserInvestigationProfileByAdminResponse(
		@JsonProperty("user_id")
		String userId,
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
		boolean isPrivate
) {
}
