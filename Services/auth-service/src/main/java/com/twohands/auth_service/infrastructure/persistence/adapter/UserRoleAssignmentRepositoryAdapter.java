package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.rbac.UserRoleAssignment;
import com.twohands.auth_service.domain.rbac.UserRoleAssignmentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserRoleAssignmentRepositoryAdapter implements UserRoleAssignmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRoleAssignmentRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserRoleAssignment> findByUserId(UUID userId) {
        String sql = """
                SELECT user_id, role_id, created_at
                FROM user_roles
                WHERE user_id = :userId
                ORDER BY created_at ASC
                """;

        var rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> new UserRoleRow(
                        (UUID) rs.getObject("user_id"),
                        (UUID) rs.getObject("role_id"),
                        toInstant(rs.getTimestamp("created_at"))
                )
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Set<UUID> roleIds = new HashSet<>();
        Instant createdAt = rows.get(0).createdAt();
        Instant updatedAt = rows.get(0).createdAt();

        for (UserRoleRow row : rows) {
            roleIds.add(row.roleId());
            if (row.createdAt() != null && (updatedAt == null || row.createdAt().isAfter(updatedAt))) {
                updatedAt = row.createdAt();
            }
        }

        return Optional.of(new UserRoleAssignment(userId, roleIds, createdAt, updatedAt));
    }

    @Override
    public UserRoleAssignment save(UserRoleAssignment assignment) {
        String deleteSql = "DELETE FROM user_roles WHERE user_id = :userId";
        jdbcTemplate.update(deleteSql, new MapSqlParameterSource("userId", assignment.userId()));

        String insertSql = """
                INSERT INTO user_roles(user_id, role_id, created_at)
                VALUES (:userId, :roleId, :createdAt)
                """;
        Instant createdAt = assignment.updatedAt() != null ? assignment.updatedAt() : Instant.now();
        for (UUID roleId : assignment.roleIds()) {
            jdbcTemplate.update(insertSql, new MapSqlParameterSource()
                    .addValue("userId", assignment.userId())
                    .addValue("roleId", roleId)
                    .addValue("createdAt", createdAt));
        }

        return assignment;
    }

    @Override
    public long countUsersByRoleId(UUID roleId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_roles WHERE role_id = :roleId",
                new MapSqlParameterSource("roleId", roleId),
                Long.class
        );
        return count == null ? 0L : count;
    }

    private Instant toInstant(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record UserRoleRow(UUID userId, UUID roleId, Instant createdAt) {
    }
}
