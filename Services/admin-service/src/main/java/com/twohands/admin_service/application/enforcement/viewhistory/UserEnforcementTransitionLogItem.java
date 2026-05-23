package com.twohands.admin_service.application.enforcement.viewhistory;

import java.time.Instant;
import java.util.UUID;

public record UserEnforcementTransitionLogItem(
		UUID logId,
		String oldStatus,
		String newStatus,
		UUID adminId,
		String actorType,
		String note,
		Instant createdAt
) {
}
