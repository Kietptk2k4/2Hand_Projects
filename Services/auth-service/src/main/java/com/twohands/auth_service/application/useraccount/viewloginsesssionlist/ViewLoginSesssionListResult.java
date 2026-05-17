package com.twohands.auth_service.application.useraccount.viewloginsesssionlist;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewLoginSesssionListResult(
        List<SessionData> sessions
) {
    public record SessionData(
            UUID id,
            String deviceId,
            String ipAddress,
            String userAgent,
            String status,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt
    ) {
    }
}
