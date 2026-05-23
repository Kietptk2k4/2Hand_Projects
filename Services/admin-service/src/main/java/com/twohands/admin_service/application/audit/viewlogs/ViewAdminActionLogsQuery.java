package com.twohands.admin_service.application.audit.viewlogs;

import java.util.UUID;

public record ViewAdminActionLogsQuery(
		UUID adminId,
		String action,
		String targetType,
		String targetId,
		String status,
		String from,
		String to,
		Integer page,
		Integer size
) {
}
