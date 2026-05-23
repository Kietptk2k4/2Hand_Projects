package com.twohands.auth_service.application.admin.viewuserinvestigationprofile;

import java.time.Instant;
import java.util.UUID;

public record ViewUserInvestigationProfileByAdminResult(
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
