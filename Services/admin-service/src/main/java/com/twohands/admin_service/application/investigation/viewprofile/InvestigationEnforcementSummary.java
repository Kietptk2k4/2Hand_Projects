package com.twohands.admin_service.application.investigation.viewprofile;

import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;

import java.time.Instant;
import java.util.UUID;

public record InvestigationEnforcementSummary(
		UUID enforcementId,
		UserEnforcementActionType actionType,
		String reasonCode,
		String status,
		Instant expiresAt,
		boolean possiblyExpired
) {
}
