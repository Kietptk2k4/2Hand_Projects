package com.twohands.admin_service.application.enforcement.banuser;

import java.time.Instant;
import java.util.UUID;

public record BanUserCommand(
		UUID userId,
		String reasonCode,
		String description,
		Instant expiresAt,
		String bearerToken
) {
}
