package com.twohands.admin_service.domain.auth;

public record AdminRefreshTokenRequest(
		String refreshToken,
		String ipAddress
) {
}
