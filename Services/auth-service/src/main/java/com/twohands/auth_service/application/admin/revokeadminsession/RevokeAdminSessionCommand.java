package com.twohands.auth_service.application.admin.revokeadminsession;

import java.util.UUID;

public record RevokeAdminSessionCommand(
        UUID actorAdminId,
        UUID sessionId,
        boolean revokeAllSessions
) {
}
