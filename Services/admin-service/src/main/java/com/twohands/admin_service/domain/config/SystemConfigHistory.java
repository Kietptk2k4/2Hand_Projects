package com.twohands.admin_service.domain.config;

import java.time.Instant;
import java.util.UUID;

public record SystemConfigHistory(
		UUID id,
		String configKey,
		String oldValue,
		String newValue,
		UUID changedBy,
		String reason,
		Instant createdAt
) {
}
