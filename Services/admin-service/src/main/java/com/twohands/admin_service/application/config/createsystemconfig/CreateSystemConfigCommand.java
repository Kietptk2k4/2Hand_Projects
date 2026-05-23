package com.twohands.admin_service.application.config.createsystemconfig;

public record CreateSystemConfigCommand(
		String configKey,
		String configValue,
		String valueType,
		String description,
		Boolean active,
		String reason
) {
}
