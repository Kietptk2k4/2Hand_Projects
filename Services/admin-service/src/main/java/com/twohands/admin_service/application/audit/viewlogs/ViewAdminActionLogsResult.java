package com.twohands.admin_service.application.audit.viewlogs;

import java.util.List;

public record ViewAdminActionLogsResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<AdminActionLogItem> logs
) {
}
