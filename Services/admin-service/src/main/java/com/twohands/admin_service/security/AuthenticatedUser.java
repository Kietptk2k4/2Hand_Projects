package com.twohands.admin_service.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
		UUID userId,
		List<String> roles,
		List<String> permissions
) {
}
