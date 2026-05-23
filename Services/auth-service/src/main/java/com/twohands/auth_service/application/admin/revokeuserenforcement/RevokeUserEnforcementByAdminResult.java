package com.twohands.auth_service.application.admin.revokeuserenforcement;

import java.util.UUID;

public record RevokeUserEnforcementByAdminResult(
        UUID userId,
        String status,
        boolean reactivated
) {
}
