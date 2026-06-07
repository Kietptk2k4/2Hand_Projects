package com.twohands.auth_service.application.admin.searchusersforinvestigation;

import java.util.UUID;

public record SearchUsersForInvestigationCommand(
		UUID actorAdminId,
		String query,
		int limit
) {
}
