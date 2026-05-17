package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RoleRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Role> findById(UUID roleId) {
        String sql = """
                SELECT id, code, name, created_at, updated_at
                FROM roles
                WHERE id = :id
                """;

        return jdbcTemplate.query(
                        sql,
                        new MapSqlParameterSource("id", roleId),
                        (rs, rowNum) -> toRole(
                                (UUID) rs.getObject("id"),
                                rs.getString("code"),
                                rs.getString("name"),
                                toInstant(rs.getTimestamp("created_at")),
                                toInstant(rs.getTimestamp("updated_at"))
                        )
                )
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Role> findByCode(String code) {
        String sql = """
                SELECT id, code, name, created_at, updated_at
                FROM roles
                WHERE code = :code
                """;

        return jdbcTemplate.query(
                        sql,
                        new MapSqlParameterSource("code", code),
                        (rs, rowNum) -> toRole(
                                (UUID) rs.getObject("id"),
                                rs.getString("code"),
                                rs.getString("name"),
                                toInstant(rs.getTimestamp("created_at")),
                                toInstant(rs.getTimestamp("updated_at"))
                        )
                )
                .stream()
                .findFirst();
    }

    @Override
    public List<Role> findAll() {
        String sql = """
                SELECT id, code, name, created_at, updated_at
                FROM roles
                ORDER BY created_at ASC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> toRole(
                        (UUID) rs.getObject("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at"))
                )
        );
    }

    @Override
    public Role save(Role role) {
        String upsertRoleSql = """
                INSERT INTO roles(id, code, name, created_at, updated_at)
                VALUES (:id, :code, :name, :createdAt, :updatedAt)
                ON CONFLICT (id) DO UPDATE
                SET code = EXCLUDED.code,
                    name = EXCLUDED.name,
                    updated_at = EXCLUDED.updated_at
                """;

        jdbcTemplate.update(upsertRoleSql, new MapSqlParameterSource()
                .addValue("id", role.id())
                .addValue("code", role.code())
                .addValue("name", role.name())
                .addValue("createdAt", role.createdAt())
                .addValue("updatedAt", role.updatedAt()));

        String deletePermissionsSql = "DELETE FROM role_permissions WHERE role_id = :roleId";
        jdbcTemplate.update(deletePermissionsSql, new MapSqlParameterSource("roleId", role.id()));

        if (!role.permissionIds().isEmpty()) {
            String insertPermissionSql = """
                    INSERT INTO role_permissions(role_id, permission_id, created_at, updated_at)
                    VALUES (:roleId, :permissionId, :createdAt, :updatedAt)
                    """;
            Instant now = Instant.now();
            for (UUID permissionId : role.permissionIds()) {
                jdbcTemplate.update(insertPermissionSql, new MapSqlParameterSource()
                        .addValue("roleId", role.id())
                        .addValue("permissionId", permissionId)
                        .addValue("createdAt", now)
                        .addValue("updatedAt", now));
            }
        }

        return role;
    }

    @Override
    public void deleteById(UUID roleId) {
        jdbcTemplate.update("DELETE FROM roles WHERE id = :id", new MapSqlParameterSource("id", roleId));
    }

    private Role toRole(UUID id, String code, String name, Instant createdAt, Instant updatedAt) {
        return new Role(id, code, name, findPermissionIdsByRoleId(id), createdAt, updatedAt);
    }

    private Set<UUID> findPermissionIdsByRoleId(UUID roleId) {
        String sql = """
                SELECT permission_id
                FROM role_permissions
                WHERE role_id = :roleId
                """;
        return new HashSet<>(jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("roleId", roleId),
                (rs, rowNum) -> (UUID) rs.getObject("permission_id")
        ));
    }

    private Instant toInstant(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
