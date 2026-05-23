package com.twohands.admin_service.application.auth.adminlogout;

public record AdminLogoutCommand(
		String refreshToken,
		String ipAddress,
		String bearerToken
) {
}
