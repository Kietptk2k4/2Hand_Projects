package com.twohands.admin_service.delivery.http.audit;

import java.util.List;

public record ViewAdminActionLogsResponse(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<AdminActionLogEntryResponse> logs
) {
}
