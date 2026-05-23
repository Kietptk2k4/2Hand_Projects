package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class PermissionQueryRepositoryAdapter implements PermissionQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PermissionQueryRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<String> findPermissionCodesByRoleIds(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT p.code
                FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.id
                WHERE rp.role_id IN (:roleIds)
                """;

        return new HashSet<>(jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("roleIds", roleIds),
                (rs, rowNum) -> rs.getString("code")
        ));
    }

    @Override
    public Set<String> findPermissionCodesByUserId(UUID userId) {
        String sql = """
                SELECT DISTINCT p.code
                FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.id
                JOIN user_roles ur ON ur.role_id = rp.role_id
                WHERE ur.user_id = :userId
                """;

        return new HashSet<>(jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> rs.getString("code")
        ));
    }

    @Override
    public List<String> findRoleCodesByUserId(UUID userId) {
        String sql = """
                SELECT DISTINCT r.code
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = :userId
                ORDER BY r.code ASC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> rs.getString("code")
        );
    }

    @Override
    public List<PermissionData> findPermissionsByRoleId(UUID roleId) {
        String sql = """
                SELECT p.code, p.description
                FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.id
                WHERE rp.role_id = :roleId
                ORDER BY p.code ASC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("roleId", roleId),
                (rs, rowNum) -> new PermissionData(
                        rs.getString("code"),
                        rs.getString("description")
                )
        );
    }
}
