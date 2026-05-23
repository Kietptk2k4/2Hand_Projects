package com.twohands.auth_service.application.admin.viewloginhistoryforadmin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewLoginHistoryForAdminResult(
        UUID userId,
        List<Item> items,
        Pagination pagination
) {
    public record Item(
            String loginMethod,
            String ipAddress,
            String userAgent,
            boolean success,
            Instant createdAt
    ) {
    }

    public record Pagination(
            int page,
            int limit,
            long totalItems,
            int totalPages,
            boolean hasNext
    ) {
    }
}
