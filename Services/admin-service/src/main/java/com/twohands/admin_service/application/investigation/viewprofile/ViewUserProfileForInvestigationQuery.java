package com.twohands.admin_service.application.investigation.viewprofile;

import java.util.UUID;

public record ViewUserProfileForInvestigationQuery(
		UUID userId,
		String bearerToken
) {
}
