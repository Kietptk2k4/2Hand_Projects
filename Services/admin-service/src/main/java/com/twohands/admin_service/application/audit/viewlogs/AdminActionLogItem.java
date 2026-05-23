package com.twohands.admin_service.application.audit.viewlogs;

import com.twohands.admin_service.domain.audit.AdminActionStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminActionLogItem(
		UUID logId,
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		AdminActionStatus status,
		String requestPayload,
		String responsePayload,
		String ipAddress,
		String userAgent,
		Instant createdAt
) {
}
