package com.twohands.admin_service.domain.auth;

public record AdminCredentialLogin(
		String email,
		String password,
		String ipAddress,
		String userAgent,
		String deviceId
) {
}
