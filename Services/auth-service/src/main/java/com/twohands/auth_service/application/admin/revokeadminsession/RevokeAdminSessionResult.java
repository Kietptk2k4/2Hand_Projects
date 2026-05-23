package com.twohands.auth_service.application.admin.revokeadminsession;

import java.util.UUID;

public record RevokeAdminSessionResult(
        UUID targetAdminUserId,
        UUID sessionId,
        int revokedSessionCount,
        boolean revokeAllSessions
) {
}
