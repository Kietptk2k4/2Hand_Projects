package com.twohands.admin_service.application.audit.logadminaction;

import java.time.Instant;
import java.util.UUID;

public record LogAdminActionResult(
		UUID logId,
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		Instant createdAt
) {
}
