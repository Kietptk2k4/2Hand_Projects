package com.twohands.auth_service.application.admin.restrictuser;

import java.time.Instant;
import java.util.UUID;

public record RestrictUserByAdminCommand(
        UUID actorAdminId,
        UUID targetUserId,
        UUID enforcementId,
        String reasonCode,
        String description,
        Instant expiresAt
) {
}
