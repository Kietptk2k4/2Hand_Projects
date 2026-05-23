package com.twohands.admin_service.application.auth.adminlogin;

public record AdminLoginCommand(
		String email,
		String password,
		String ipAddress,
		String userAgent,
		String deviceId
) {
}
