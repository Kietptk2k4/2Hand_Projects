package com.twohands.admin_service.application.enforcement.viewhistory;

import java.util.UUID;

public record ViewUserEnforcementHistoryQuery(
		UUID userId,
		Integer page,
		Integer size
) {
}
