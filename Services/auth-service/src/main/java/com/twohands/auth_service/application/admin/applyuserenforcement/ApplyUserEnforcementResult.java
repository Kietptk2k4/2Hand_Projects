package com.twohands.auth_service.application.admin.applyuserenforcement;

import java.util.UUID;

public record ApplyUserEnforcementResult(
        UUID userId,
        String status,
        int revokedSessionCount,
        boolean idempotentReplay,
        boolean reactivated
) {
}
