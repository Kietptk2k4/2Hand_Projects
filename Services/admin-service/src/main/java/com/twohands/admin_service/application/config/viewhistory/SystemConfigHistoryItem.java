package com.twohands.admin_service.application.config.viewhistory;

import java.time.Instant;
import java.util.UUID;

public record SystemConfigHistoryItem(
		UUID historyId,
		String configKey,
		String oldValue,
		String newValue,
		UUID changedBy,
		String reason,
		Instant createdAt,
		boolean valuesMasked
) {
}
