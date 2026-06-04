package com.twohands.auth_service.infrastructure.persistence;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Converts {@link Instant} for {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate}
 * bindings. PostgreSQL JDBC cannot infer SQL types for {@link Instant} directly.
 */
public final class JdbcTimestamps {

    private JdbcTimestamps() {
    }

    public static Timestamp from(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
