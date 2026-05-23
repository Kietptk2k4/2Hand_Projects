package com.twohands.admin_service.domain.integration;

import java.time.Instant;
import java.util.UUID;

public record InvestigationUserProfile(
		UUID userId,
		String email,
		String status,
		boolean emailVerified,
		boolean phoneVerified,
		Instant lastLoginAt,
		Instant createdAt,
		String displayName,
		String avatarUrl,
		String bio,
		String website,
		boolean isPrivate
) {
}
