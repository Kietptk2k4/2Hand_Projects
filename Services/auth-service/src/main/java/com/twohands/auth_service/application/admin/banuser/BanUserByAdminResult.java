package com.twohands.auth_service.application.admin.banuser;

import java.util.UUID;

public record BanUserByAdminResult(
        UUID userId,
        String status,
        int revokedSessionCount
) {
}
