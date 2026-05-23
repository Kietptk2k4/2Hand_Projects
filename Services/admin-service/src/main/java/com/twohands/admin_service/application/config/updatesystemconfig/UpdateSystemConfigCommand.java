package com.twohands.admin_service.application.config.updatesystemconfig;

import java.util.UUID;

public record UpdateSystemConfigCommand(
		UUID configId,
		String configValue,
		String description,
		String reason
) {
}
