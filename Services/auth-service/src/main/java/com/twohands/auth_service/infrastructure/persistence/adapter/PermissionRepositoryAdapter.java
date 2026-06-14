package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.rbac.Permission;
import com.twohands.auth_service.domain.rbac.PermissionRepository;
import com.twohands.auth_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PermissionRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Permission> findById(UUID permissionId) {
        String sql = """
                SELECT id, code, description, created_at, updated_at
                FROM permissions
                WHERE id = :id
                """;

        return jdbcTemplate.query(
                        sql,
                        new MapSqlParameterSource("id", permissionId),
                        (rs, rowNum) -> toPermission(
                                (UUID) rs.getObject("id"),
                                rs.getString("code"),
                                rs.getString("description"),
                                rs.getTimestamp("created_at").toInstant(),
                                rs.getTimestamp("updated_at").toInstant()
                        )
                )
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        String sql = """
                SELECT id, code, description, created_at, updated_at
                FROM permissions
                WHERE code = :code
                """;

        return jdbcTemplate.query(
                        sql,
                        new MapSqlParameterSource("code", code),
                        (rs, rowNum) -> toPermission(
                                (UUID) rs.getObject("id"),
                                rs.getString("code"),
                                rs.getString("description"),
                                rs.getTimestamp("created_at").toInstant(),
                                rs.getTimestamp("updated_at").toInstant()
                        )
                )
                .stream()
                .findFirst();
    }

    @Override
    public Permission save(Permission permission) {
        String sql = """
                INSERT INTO permissions(id, code, description, created_at, updated_at)
                VALUES (:id, :code, :description, :createdAt, :updatedAt)
                ON CONFLICT (code) DO UPDATE
                SET description = EXCLUDED.description,
                    updated_at = EXCLUDED.updated_at
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", permission.id())
                .addValue("code", permission.code())
                .addValue("description", permission.description())
                .addValue("createdAt", JdbcTimestamps.from(permission.createdAt()))
                .addValue("updatedAt", JdbcTimestamps.from(permission.updatedAt())));

        return permission;
    }

    @Override
    public void deleteById(UUID permissionId) {
        jdbcTemplate.update(
                "DELETE FROM permissions WHERE id = :id",
                new MapSqlParameterSource("id", permissionId)
        );
    }

    private Permission toPermission(UUID id, String code, String description, Instant createdAt, Instant updatedAt) {
        return new Permission(id, code, description, createdAt, updatedAt);
    }
}
