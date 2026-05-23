package com.twohands.auth_service.application.admin.viewusersessionsforadmin;

import java.util.UUID;

public record ViewUserSessionsForAdminCommand(
        UUID actorAdminId,
        UUID targetUserId,
        String status,
        int page,
        int limit
) {
}
