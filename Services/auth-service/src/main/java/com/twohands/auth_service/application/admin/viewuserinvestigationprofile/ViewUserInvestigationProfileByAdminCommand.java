package com.twohands.auth_service.application.admin.viewuserinvestigationprofile;

import java.util.UUID;

public record ViewUserInvestigationProfileByAdminCommand(
		UUID actorAdminId,
		UUID targetUserId
) {
}
