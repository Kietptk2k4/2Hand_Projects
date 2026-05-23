package com.twohands.admin_service.application.config.updatesystemconfig;

import com.twohands.admin_service.domain.config.SystemConfigValueType;

import java.time.Instant;
import java.util.UUID;

public record UpdateSystemConfigResult(
		UUID configId,
		String configKey,
		String configValue,
		SystemConfigValueType valueType,
		String description,
		boolean active,
		UUID updatedBy,
		Instant updatedAt,
		UUID historyId,
		UUID outboxEventId
) {
}
