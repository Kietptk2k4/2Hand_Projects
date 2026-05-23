package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminLoginResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("refresh_token")
		String refreshToken,
		@JsonProperty("expires_in")
		long expiresIn,
		AdminUserInfo user,
		List<String> roles,
		List<String> permissions
) {
	public record AdminUserInfo(
			String id,
			String email,
			String status
	) {
	}
}
