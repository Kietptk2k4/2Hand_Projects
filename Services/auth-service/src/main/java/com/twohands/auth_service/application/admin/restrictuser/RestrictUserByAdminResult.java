package com.twohands.auth_service.application.admin.restrictuser;

import java.util.UUID;

public record RestrictUserByAdminResult(
        UUID userId,
        String status,
        int revokedSessionCount
) {
}
