package com.twohands.admin_service.domain.audit;

import java.time.Instant;
import java.util.UUID;

public record AdminActionLogSearchCriteria(
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		AdminActionStatus status,
		Instant from,
		Instant to
) {
}
