package com.twohands.admin_service.application.enforcement.viewhistory;

import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserEnforcementHistoryItem(
		UUID enforcementId,
		UUID userId,
		UserEnforcementActionType actionType,
		String reasonCode,
		String description,
		Instant expiresAt,
		UUID enforcedBy,
		UserEnforcementStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<UserEnforcementTransitionLogItem> logs
) {
}
