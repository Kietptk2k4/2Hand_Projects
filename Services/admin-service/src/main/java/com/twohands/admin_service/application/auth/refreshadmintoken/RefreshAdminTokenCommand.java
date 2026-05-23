package com.twohands.admin_service.application.auth.refreshadmintoken;

public record RefreshAdminTokenCommand(
		String refreshToken,
		String ipAddress
) {
}
