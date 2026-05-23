package com.twohands.auth_service.application.admin.revokeuserenforcement;

import java.util.UUID;

public record RevokeUserEnforcementByAdminCommand(
        UUID actorAdminId,
        UUID enforcementId,
        UUID userId,
        String actionType,
        boolean reactivateUser,
        String note,
        String reason
) {
}
