package com.twohands.admin_service.application.enforcement.restrictuser;

import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;

import java.time.Instant;
import java.util.UUID;

public record RestrictUserResult(
		UUID enforcementId,
		UUID userId,
		String reasonCode,
		UserEnforcementStatus status,
		Instant expiresAt,
		UUID enforcedBy,
		Instant createdAt,
		UUID outboxEventId
) {
}
