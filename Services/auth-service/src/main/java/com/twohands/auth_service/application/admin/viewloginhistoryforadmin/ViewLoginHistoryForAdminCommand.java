package com.twohands.auth_service.application.admin.viewloginhistoryforadmin;

import java.util.UUID;

public record ViewLoginHistoryForAdminCommand(
        UUID actorAdminId,
        UUID targetUserId,
        int page,
        int limit,
        Boolean success,
        String from,
        String to
) {
}
