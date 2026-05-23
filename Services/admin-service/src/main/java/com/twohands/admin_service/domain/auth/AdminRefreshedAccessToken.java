package com.twohands.admin_service.domain.auth;

import java.util.List;
import java.util.UUID;

public record AdminRefreshedAccessToken(
		String accessToken,
		long expiresIn,
		UUID adminId,
		String email,
		String status,
		List<String> roles,
		List<String> permissions
) {
}
