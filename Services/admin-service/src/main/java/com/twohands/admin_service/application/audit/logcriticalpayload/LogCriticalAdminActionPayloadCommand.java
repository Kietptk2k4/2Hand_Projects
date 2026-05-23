package com.twohands.admin_service.application.audit.logcriticalpayload;

import com.twohands.admin_service.domain.audit.AdminActionStatus;

import java.util.Map;
import java.util.UUID;

public record LogCriticalAdminActionPayloadCommand(
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		AdminActionStatus status,
		String message,
		String summary,
		Map<String, Object> before,
		Map<String, Object> after,
		Map<String, Object> additionalContext,
		Map<String, Object> resultSummary
) {
}
