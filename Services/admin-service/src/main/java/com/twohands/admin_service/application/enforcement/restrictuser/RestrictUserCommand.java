package com.twohands.admin_service.application.enforcement.restrictuser;

import java.time.Instant;
import java.util.UUID;

public record RestrictUserCommand(
		UUID userId,
		String reasonCode,
		String description,
		Instant expiresAt,
		String bearerToken
) {
}
