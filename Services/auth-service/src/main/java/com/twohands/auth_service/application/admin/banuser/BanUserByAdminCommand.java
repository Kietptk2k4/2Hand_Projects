package com.twohands.auth_service.application.admin.banuser;

import java.time.Instant;
import java.util.UUID;

public record BanUserByAdminCommand(
        UUID actorAdminId,
        UUID targetUserId,
        UUID enforcementId,
        String reasonCode,
        String description,
        Instant expiresAt
) {
}
