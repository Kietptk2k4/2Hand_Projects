package com.twohands.auth_service.application.admin.viewusersessionsforadmin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewUserSessionsForAdminResult(
        UUID userId,
        List<SessionItem> sessions,
        Pagination pagination
) {
    public record SessionItem(
            UUID sessionId,
            String deviceId,
            String ipAddress,
            String userAgent,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record Pagination(
            int page,
            int limit,
            long totalItems,
            boolean hasNext
    ) {
    }
}
