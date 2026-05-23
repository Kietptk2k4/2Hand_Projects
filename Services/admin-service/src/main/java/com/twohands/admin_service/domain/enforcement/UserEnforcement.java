package com.twohands.admin_service.domain.enforcement;

import java.time.Instant;
import java.util.UUID;

public record UserEnforcement(
		UUID id,
		UUID userId,
		UserEnforcementActionType actionType,
		String reasonCode,
		String description,
		Instant expiresAt,
		UUID enforcedBy,
		UserEnforcementStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
