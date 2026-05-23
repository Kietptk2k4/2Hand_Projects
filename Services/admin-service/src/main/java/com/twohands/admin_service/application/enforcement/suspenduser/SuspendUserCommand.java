package com.twohands.admin_service.application.enforcement.suspenduser;

import java.time.Instant;
import java.util.UUID;

public record SuspendUserCommand(
		UUID userId,
		String reasonCode,
		String description,
		Instant expiresAt,
		String bearerToken
) {
}
