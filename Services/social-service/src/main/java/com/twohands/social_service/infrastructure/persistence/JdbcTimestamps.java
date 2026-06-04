package com.twohands.social_service.infrastructure.persistence;

import java.sql.Timestamp;
import java.time.Instant;

public final class JdbcTimestamps {

    private JdbcTimestamps() {
    }

    public static Timestamp from(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
