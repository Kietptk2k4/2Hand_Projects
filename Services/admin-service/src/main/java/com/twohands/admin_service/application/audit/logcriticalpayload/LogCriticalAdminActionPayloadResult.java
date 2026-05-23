package com.twohands.admin_service.application.audit.logcriticalpayload;

import java.time.Instant;
import java.util.UUID;

public record LogCriticalAdminActionPayloadResult(
		UUID logId,
		UUID adminId,
		String actionType,
		String requestPayloadJson,
		Instant createdAt
) {
}
