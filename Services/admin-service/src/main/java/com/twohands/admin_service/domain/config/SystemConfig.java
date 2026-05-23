package com.twohands.admin_service.domain.config;

import java.time.Instant;
import java.util.UUID;

public record SystemConfig(
		UUID id,
		String configKey,
		String configValue,
		SystemConfigValueType valueType,
		String description,
		boolean active,
		UUID createdBy,
		Instant createdAt,
		UUID updatedBy,
		Instant updatedAt
) {
}
