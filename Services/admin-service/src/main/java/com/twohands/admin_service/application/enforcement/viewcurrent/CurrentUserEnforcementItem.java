package com.twohands.admin_service.application.enforcement.viewcurrent;

import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;

import java.time.Instant;
import java.util.UUID;

public record CurrentUserEnforcementItem(
		UUID enforcementId,
		UUID userId,
		UserEnforcementActionType actionType,
		String reasonCode,
		String description,
		Instant expiresAt,
		UUID enforcedBy,
		Instant createdAt,
		boolean possiblyExpired
) {
}
