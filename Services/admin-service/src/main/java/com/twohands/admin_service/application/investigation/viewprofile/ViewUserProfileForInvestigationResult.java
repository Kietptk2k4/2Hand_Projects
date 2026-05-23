package com.twohands.admin_service.application.investigation.viewprofile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewUserProfileForInvestigationResult(
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
		boolean isPrivate,
		List<InvestigationEnforcementSummary> currentEnforcements
) {
}
