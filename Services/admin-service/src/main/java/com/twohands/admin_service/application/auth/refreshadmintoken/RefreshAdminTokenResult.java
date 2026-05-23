package com.twohands.admin_service.application.auth.refreshadmintoken;

import java.util.List;
import java.util.UUID;

public record RefreshAdminTokenResult(
		String accessToken,
		long expiresIn,
		UUID adminId,
		String email,
		String status,
		List<String> roles,
		List<String> permissions
) {
}
