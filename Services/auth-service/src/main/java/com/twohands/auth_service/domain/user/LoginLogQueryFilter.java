package com.twohands.auth_service.domain.user;

import java.time.Instant;

public record LoginLogQueryFilter(
        Boolean success,
        Instant from,
        Instant to
) {
    public static LoginLogQueryFilter empty() {
        return new LoginLogQueryFilter(null, null, null);
    }
}
