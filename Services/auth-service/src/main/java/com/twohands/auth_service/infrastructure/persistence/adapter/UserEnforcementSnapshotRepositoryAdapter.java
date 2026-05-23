package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshot;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotRepository;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserEnforcementSnapshotRepositoryAdapter implements UserEnforcementSnapshotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserEnforcementSnapshotRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserEnforcementSnapshot> findByEnforcementId(UUID enforcementId) {
        String sql = """
                SELECT enforcement_id, user_id, action_type, status, reason_code, description,
                       expires_at, event_id, applied_at, created_at, updated_at
                FROM user_enforcement_snapshots
                WHERE enforcement_id = :enforcementId
                """;
        return querySingle(sql, new MapSqlParameterSource("enforcementId", enforcementId));
    }

    @Override
    public Optional<UserEnforcementSnapshot> findByEventId(UUID eventId) {
        String sql = """
                SELECT enforcement_id, user_id, action_type, status, reason_code, description,
                       expires_at, event_id, applied_at, created_at, updated_at
                FROM user_enforcement_snapshots
                WHERE event_id = :eventId
                """;
        return querySingle(sql, new MapSqlParameterSource("eventId", eventId));
    }

    @Override
    public boolean existsAppliedBlockingEnforcement(UUID userId) {
        String sql = """
                SELECT COUNT(*)
                FROM user_enforcement_snapshots
                WHERE user_id = :userId
                  AND status = :appliedStatus
                  AND action_type IN ('SUSPEND', 'BAN')
                """;
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("appliedStatus", UserEnforcementSnapshotStatus.APPLIED.name()), Long.class);
        return count != null && count > 0;
    }

    @Override
    public UserEnforcementSnapshot save(UserEnforcementSnapshot snapshot) {
        String sql = """
                INSERT INTO user_enforcement_snapshots(
                    enforcement_id, user_id, action_type, status, reason_code, description,
                    expires_at, event_id, applied_at, created_at, updated_at
                )
                VALUES (
                    :enforcementId, :userId, :actionType, :status, :reasonCode, :description,
                    :expiresAt, :eventId, :appliedAt, :createdAt, :updatedAt
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("enforcementId", snapshot.enforcementId())
                .addValue("userId", snapshot.userId())
                .addValue("actionType", snapshot.actionType().name())
                .addValue("status", snapshot.status().name())
                .addValue("reasonCode", snapshot.reasonCode())
                .addValue("description", snapshot.description())
                .addValue("expiresAt", snapshot.expiresAt())
                .addValue("eventId", snapshot.eventId())
                .addValue("appliedAt", snapshot.appliedAt())
                .addValue("createdAt", snapshot.createdAt())
                .addValue("updatedAt", snapshot.updatedAt());
        jdbcTemplate.update(sql, params);
        return snapshot;
    }

    @Override
    public int markStatus(UUID enforcementId, UserEnforcementSnapshotStatus status, Instant updatedAt) {
        String sql = """
                UPDATE user_enforcement_snapshots
                SET status = :status, updated_at = :updatedAt
                WHERE enforcement_id = :enforcementId
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("updatedAt", Timestamp.from(updatedAt))
                .addValue("enforcementId", enforcementId));
    }

    private Optional<UserEnforcementSnapshot> querySingle(String sql, MapSqlParameterSource params) {
        List<UserEnforcementSnapshot> rows = jdbcTemplate.query(sql, params, (rs, rowNum) -> mapSnapshot(rs));
        return rows.stream().findFirst();
    }

    private UserEnforcementSnapshot mapSnapshot(ResultSet rs) throws SQLException {
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        String eventId = rs.getString("event_id");
        return new UserEnforcementSnapshot(
                UUID.fromString(rs.getString("enforcement_id")),
                UUID.fromString(rs.getString("user_id")),
                UserEnforcementActionType.valueOf(rs.getString("action_type")),
                UserEnforcementSnapshotStatus.valueOf(rs.getString("status")),
                rs.getString("reason_code"),
                rs.getString("description"),
                expiresAt == null ? null : expiresAt.toInstant(),
                eventId == null ? null : UUID.fromString(eventId),
                rs.getTimestamp("applied_at").toInstant(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
