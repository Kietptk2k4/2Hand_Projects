package com.twohands.auth_service.application.admin.suspenduser;

import java.time.Instant;
import java.util.UUID;

public record SuspendUserByAdminCommand(
        UUID actorAdminId,
        UUID targetUserId,
        UUID enforcementId,
        String reasonCode,
        String description,
        Instant expiresAt
) {
}
