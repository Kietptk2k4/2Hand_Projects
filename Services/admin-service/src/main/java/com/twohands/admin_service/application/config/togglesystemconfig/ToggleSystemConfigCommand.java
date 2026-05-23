package com.twohands.admin_service.application.config.togglesystemconfig;

import java.util.UUID;

public record ToggleSystemConfigCommand(
		UUID configId,
		boolean active,
		String reason
) {
}
