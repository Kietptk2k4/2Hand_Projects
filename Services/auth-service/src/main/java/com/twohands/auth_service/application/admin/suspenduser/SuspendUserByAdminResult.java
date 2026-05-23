package com.twohands.auth_service.application.admin.suspenduser;

import java.util.UUID;

public record SuspendUserByAdminResult(
        UUID userId,
        String status,
        int revokedSessionCount
) {
}
