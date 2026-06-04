package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionPage;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.infrastructure.persistence.JdbcPgEnumTypes;
import com.twohands.auth_service.infrastructure.persistence.JdbcSqlDialect;
import com.twohands.auth_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenSessionRepositoryAdapter implements RefreshTokenSessionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcSqlDialect sqlDialect;

    public RefreshTokenSessionRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate, JdbcSqlDialect sqlDialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlDialect = sqlDialect;
    }

    @Override
    public Optional<RefreshTokenSession> findById(UUID sessionId) {
        String sql = """
                SELECT id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                FROM refresh_token_sessions
                WHERE id = :id
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", sessionId), (rs, rowNum) -> mapSession(rs))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<RefreshTokenSession> findByTokenHash(String tokenHash) {
        String sql = """
                SELECT id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                FROM refresh_token_sessions
                WHERE token_hash = :tokenHash
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("tokenHash", tokenHash), (rs, rowNum) -> mapSession(rs))
                .stream()
                .findFirst();
    }

    @Override
    public List<RefreshTokenSession> findByUserIdAndStatus(UUID userId, SessionStatus status) {
        String sql = """
                SELECT id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                FROM refresh_token_sessions
                WHERE user_id = :userId AND status = %s
                ORDER BY created_at DESC
                """.formatted(sqlDialect.castEnum("status", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("status", status.name());
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapSession(rs));
    }

    @Override
    public RefreshTokenSessionPage findPageByUserId(UUID userId, SessionStatus statusFilter, int limit, int offset) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit)
                .addValue("offset", offset);

        String statusClause = statusFilter == null
                ? ""
                : " AND status = " + sqlDialect.castEnum("status", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS) + " ";
        if (statusFilter != null) {
            params.addValue("status", statusFilter.name());
        }

        String countSql = """
                SELECT COUNT(*)
                FROM refresh_token_sessions
                WHERE user_id = :userId
                """ + statusClause;

        Long totalItems = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (totalItems == null) {
            totalItems = 0L;
        }

        String querySql = """
                SELECT id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                FROM refresh_token_sessions
                WHERE user_id = :userId
                """ + statusClause + """
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        List<RefreshTokenSession> sessions = jdbcTemplate.query(querySql, params, (rs, rowNum) -> mapSession(rs));
        return new RefreshTokenSessionPage(sessions, totalItems);
    }

    @Override
    public RefreshTokenSession save(RefreshTokenSession session) {
        String sql = """
                INSERT INTO refresh_token_sessions(
                    id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                )
                VALUES (
                    :id, :userId, :tokenHash, :deviceId, :ipAddress, :userAgent, :expiresAt, %s, :createdAt, :updatedAt
                )
                """.formatted(sqlDialect.castEnum("status", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", session.id())
                .addValue("userId", session.userId())
                .addValue("tokenHash", session.tokenHash())
                .addValue("deviceId", session.deviceId())
                .addValue("ipAddress", session.ipAddress())
                .addValue("userAgent", session.userAgent())
                .addValue("expiresAt", JdbcTimestamps.from(session.expiresAt()))
                .addValue("status", session.status().name())
                .addValue("createdAt", JdbcTimestamps.from(session.createdAt()))
                .addValue("updatedAt", JdbcTimestamps.from(session.updatedAt()));

        jdbcTemplate.update(sql, params);
        return session;
    }

    @Override
    public int markLoggedOutIfActive(UUID sessionId, Instant updatedAt) {
        String sql = """
                UPDATE refresh_token_sessions
                SET status = %s, updated_at = :updatedAt
                WHERE id = :sessionId AND status = %s
                """.formatted(
                sqlDialect.castEnum("loggedOutStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS),
                sqlDialect.castEnum("activeStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS)
        );
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("loggedOutStatus", SessionStatus.LOGGED_OUT.name())
                .addValue("updatedAt", JdbcTimestamps.from(updatedAt))
                .addValue("sessionId", sessionId)
                .addValue("activeStatus", SessionStatus.ACTIVE.name()));
    }

    @Override
    public int markRevokedIfActive(UUID sessionId, Instant updatedAt) {
        String sql = """
                UPDATE refresh_token_sessions
                SET status = %s, updated_at = :updatedAt
                WHERE id = :sessionId AND status = %s
                """.formatted(
                sqlDialect.castEnum("revokedStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS),
                sqlDialect.castEnum("activeStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS)
        );
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("revokedStatus", SessionStatus.REVOKED.name())
                .addValue("updatedAt", JdbcTimestamps.from(updatedAt))
                .addValue("sessionId", sessionId)
                .addValue("activeStatus", SessionStatus.ACTIVE.name()));
    }

    @Override
    public int revokeAllByUserId(UUID userId) {
        String sql = """
                UPDATE refresh_token_sessions
                SET status = %s, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = :userId AND status = %s
                """.formatted(
                sqlDialect.castEnum("revokedStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS),
                sqlDialect.castEnum("activeStatus", JdbcPgEnumTypes.REFRESH_TOKEN_STATUS)
        );
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("revokedStatus", SessionStatus.REVOKED.name())
                .addValue("activeStatus", SessionStatus.ACTIVE.name())
                .addValue("userId", userId));
    }

    private RefreshTokenSession mapSession(ResultSet rs) throws SQLException {
        return new RefreshTokenSession(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("token_hash"),
                rs.getString("device_id"),
                rs.getString("ip_address"),
                rs.getString("user_agent"),
                rs.getTimestamp("expires_at").toInstant(),
                SessionStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
