package com.twohands.admin_service.application.config.getsystemconfig;

import com.twohands.admin_service.domain.config.SystemConfigValueType;

import java.time.Instant;
import java.util.UUID;

public record GetSystemConfigResult(
		UUID configId,
		String configKey,
		String configValue,
		SystemConfigValueType valueType,
		String description,
		boolean active,
		UUID createdBy,
		Instant createdAt,
		UUID updatedBy,
		Instant updatedAt,
		boolean valueMasked
) {
}
