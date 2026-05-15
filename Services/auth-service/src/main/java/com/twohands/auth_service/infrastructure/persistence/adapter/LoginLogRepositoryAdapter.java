package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.LoginMethod;
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
                .addValue("createdAt", log.createdAt());
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

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new LoginLog(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                LoginMethod.valueOf(rs.getString("login_method")),
                rs.getString("ip_address"),
                rs.getString("user_agent"),
                rs.getBoolean("success"),
                rs.getTimestamp("created_at").toInstant()
        ));
    }
}
