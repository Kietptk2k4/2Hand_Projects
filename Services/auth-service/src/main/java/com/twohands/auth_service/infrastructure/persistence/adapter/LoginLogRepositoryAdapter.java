package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogPage;
import com.twohands.auth_service.domain.user.LoginLogQueryFilter;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.LoginMethod;
import com.twohands.auth_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LoginLogRepositoryAdapter implements LoginLogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LoginLogRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginLog save(LoginLog log) {
        String sql = """
                INSERT INTO login_logs(id, user_id, login_method, ip_address, user_agent, success, created_at)
                VALUES (:id, :userId, :loginMethod, :ipAddress, :userAgent, :success, :createdAt)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", log.id())
                .addValue("userId", log.userId())
                .addValue("loginMethod", log.loginMethod().name())
                .addValue("ipAddress", log.ipAddress())
                .addValue("userAgent", log.userAgent())
                .addValue("success", log.success())
                .addValue("createdAt", JdbcTimestamps.from(log.createdAt()));
        jdbcTemplate.update(sql, params);
        return log;
    }

    @Override
    public List<LoginLog> findByUserId(UUID userId, int limit, int offset) {
        String sql = """
                SELECT id, user_id, login_method, ip_address, user_agent, success, created_at
                FROM login_logs
                WHERE user_id = :userId
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapLoginLog(rs));
    }

    @Override
    public LoginLogPage findPageByUserId(UUID userId, LoginLogQueryFilter filter, int limit, int offset) {
        LoginLogQueryFilter effectiveFilter = filter == null ? LoginLogQueryFilter.empty() : filter;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit)
                .addValue("offset", offset);

        StringBuilder whereClause = new StringBuilder(" WHERE user_id = :userId ");
        if (effectiveFilter.success() != null) {
            whereClause.append(" AND success = :success ");
            params.addValue("success", effectiveFilter.success());
        }
        if (effectiveFilter.from() != null) {
            whereClause.append(" AND created_at >= :from ");
            params.addValue("from", JdbcTimestamps.from(effectiveFilter.from()));
        }
        if (effectiveFilter.to() != null) {
            whereClause.append(" AND created_at <= :to ");
            params.addValue("to", JdbcTimestamps.from(effectiveFilter.to()));
        }

        String countSql = "SELECT COUNT(*) FROM login_logs " + whereClause;
        Long totalItems = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (totalItems == null) {
            totalItems = 0L;
        }

        String querySql = """
                SELECT id, user_id, login_method, ip_address, user_agent, success, created_at
                FROM login_logs
                """ + whereClause + """
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        List<LoginLog> items = jdbcTemplate.query(querySql, params, (rs, rowNum) -> mapLoginLog(rs));
        return new LoginLogPage(items, totalItems);
    }

    private LoginLog mapLoginLog(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new LoginLog(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                LoginMethod.valueOf(rs.getString("login_method")),
                rs.getString("ip_address"),
                rs.getString("user_agent"),
                rs.getBoolean("success"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
