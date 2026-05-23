package com.twohands.admin_service.application.enforcement.revokeuserenforcement;

import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;

import java.time.Instant;
import java.util.UUID;

public record RevokeUserEnforcementResult(
		UUID enforcementId,
		UUID userId,
		UserEnforcementActionType actionType,
		UserEnforcementStatus status,
		UUID revokedBy,
		Instant updatedAt,
		UUID outboxEventId
) {
}
