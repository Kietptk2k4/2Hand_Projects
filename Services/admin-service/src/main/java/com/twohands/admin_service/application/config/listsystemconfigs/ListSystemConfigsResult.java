package com.twohands.admin_service.application.config.listsystemconfigs;

import java.util.List;

public record ListSystemConfigsResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<SystemConfigListItem> items
) {
}
