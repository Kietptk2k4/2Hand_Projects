package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RefreshAdminTokenResponse(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("expires_in")
		long expiresIn,
		AdminLoginResponse.AdminUserInfo user,
		List<String> roles,
		List<String> permissions
) {
}
