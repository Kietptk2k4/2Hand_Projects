package com.twohands.admin_service.domain.auth;

public record AdminLogoutDelegation(
		String refreshToken,
		String ipAddress,
		String bearerToken
) {
}
