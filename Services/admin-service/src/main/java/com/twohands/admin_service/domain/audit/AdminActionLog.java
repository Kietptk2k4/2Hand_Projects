package com.twohands.admin_service.domain.audit;

import java.time.Instant;
import java.util.UUID;

public record AdminActionLog(
		UUID id,
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		AdminActionStatus status,
		String requestPayloadJson,
		String responsePayloadJson,
		String ipAddress,
		String userAgent,
		Instant createdAt
) {
}
