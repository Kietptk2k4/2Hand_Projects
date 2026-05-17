package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.infrastructure.persistence.mapper.UserJdbcMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserJdbcMapper jdbcMapper;

    public UserRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate, UserJdbcMapper jdbcMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcMapper = jdbcMapper;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        String sql = """
                SELECT id, email, password_hash, status, email_verified, phone_verified,
                       last_login_at, password_changed_at, deleted_at, created_at, updated_at
                FROM users
                WHERE id = :id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", userId), (rs, rowNum) -> jdbcMapper.toUser(rs))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findByEmailNormalized(String emailNormalized) {
        String sql = """
                SELECT id, email, password_hash, status, email_verified, phone_verified,
                       last_login_at, password_changed_at, deleted_at, created_at, updated_at
                FROM users
                WHERE email_normalized = :emailNormalized
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("emailNormalized", emailNormalized), (rs, rowNum) -> jdbcMapper.toUser(rs))
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByEmailNormalized(String emailNormalized) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE email_normalized = :emailNormalized)";
        Boolean exists = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("emailNormalized", emailNormalized), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users(id, email, email_normalized, password_hash, status, email_verified, phone_verified, created_at, updated_at)
                VALUES (:id, :email, :emailNormalized, :passwordHash, :status, :emailVerified, :phoneVerified, :createdAt, :updatedAt)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", user.id())
                .addValue("email", user.email().value())
                .addValue("emailNormalized", user.email().normalizedValue())
                .addValue("passwordHash", user.passwordHash().value())
                .addValue("status", user.status().name())
                .addValue("emailVerified", user.emailVerified())
                .addValue("phoneVerified", user.phoneVerified())
                .addValue("createdAt", user.createdAt())
                .addValue("updatedAt", user.updatedAt());

        jdbcTemplate.update(sql, params);
        return user;
    }

    @Override
    public void deleteById(UUID userId) {
        jdbcTemplate.update("DELETE FROM users WHERE id = :id", new MapSqlParameterSource("id", userId));
    }

    @Override
    public int countByStatus(UserStatus status) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE status = :status",
                new MapSqlParameterSource("status", status.name()),
                Integer.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public void updateLastLoginAt(UUID userId, Instant lastLoginAt) {
        String sql = "UPDATE users SET last_login_at = :lastLoginAt, updated_at = :updatedAt WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", userId)
                .addValue("lastLoginAt", lastLoginAt)
                .addValue("updatedAt", lastLoginAt));
    }

    @Override
    public void updatePassword(UUID userId, PasswordHash passwordHash, Instant passwordChangedAt) {
        String sql = """
                UPDATE users
                SET password_hash = :passwordHash,
                    password_changed_at = :passwordChangedAt,
                    updated_at = :updatedAt
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", userId)
                .addValue("passwordHash", passwordHash.value())
                .addValue("passwordChangedAt", passwordChangedAt)
                .addValue("updatedAt", passwordChangedAt));
    }

    @Override
    public void updateStatusDeleted(UUID userId, Instant deletedAt) {
        String sql = """
                UPDATE users
                SET status = :status,
                    deleted_at = :deletedAt,
                    updated_at = :updatedAt
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", userId)
                .addValue("status", "DELETED")
                .addValue("deletedAt", deletedAt)
                .addValue("updatedAt", deletedAt));
    }
}
