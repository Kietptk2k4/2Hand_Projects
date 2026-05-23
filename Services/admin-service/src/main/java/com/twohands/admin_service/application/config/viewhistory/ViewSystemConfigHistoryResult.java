package com.twohands.admin_service.application.config.viewhistory;

import java.util.List;
import java.util.UUID;

public record ViewSystemConfigHistoryResult(
		UUID configId,
		String configKey,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean valuesMasked,
		List<SystemConfigHistoryItem> history
) {
}
