package com.twohands.admin_service.application.enforcement.viewhistory;

import java.util.List;
import java.util.UUID;

public record ViewUserEnforcementHistoryResult(
		UUID userId,
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<UserEnforcementHistoryItem> enforcements
) {
}
