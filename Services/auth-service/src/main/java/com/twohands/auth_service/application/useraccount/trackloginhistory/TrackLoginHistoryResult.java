package com.twohands.auth_service.application.useraccount.trackloginhistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrackLoginHistoryResult(
        List<Item> items,
        int limit,
        int offset
) {
    public record Item(
            UUID id,
            String loginMethod,
            String ipAddress,
            String userAgent,
            boolean success,
            Instant createdAt
    ) {
    }
}
