package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VerificationTokenRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationTokenType type) {
        String sql = """
                SELECT id, user_id, token_hash, type, expires_at, used_at, created_at
                FROM verification_tokens
                WHERE token_hash = :tokenHash AND type = :type
                """;

        return jdbcTemplate.query(sql,
                        new MapSqlParameterSource()
                                .addValue("tokenHash", tokenHash)
                                .addValue("type", type.name()),
                        (rs, rowNum) -> new VerificationToken(
                                UUID.fromString(rs.getString("id")),
                                UUID.fromString(rs.getString("user_id")),
                                rs.getString("token_hash"),
                                VerificationTokenType.valueOf(rs.getString("type")),
                                rs.getTimestamp("expires_at").toInstant(),
                                rs.getTimestamp("used_at") == null ? null : rs.getTimestamp("used_at").toInstant(),
                                rs.getTimestamp("created_at").toInstant()
                        ))
                .stream()
                .findFirst();
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        String sql = """
                INSERT INTO verification_tokens(id, user_id, token_hash, type, expires_at, used_at, created_at)
                VALUES (:id, :userId, :tokenHash, :type, :expiresAt, :usedAt, :createdAt)
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", token.id())
                .addValue("userId", token.userId())
                .addValue("tokenHash", token.tokenHash())
                .addValue("type", token.type().name())
                .addValue("expiresAt", token.expiresAt())
                .addValue("usedAt", token.usedAt())
                .addValue("createdAt", token.createdAt()));

        return token;
    }

    @Override
    public void deleteById(UUID tokenId) {
        jdbcTemplate.update("DELETE FROM verification_tokens WHERE id = :id", new MapSqlParameterSource("id", tokenId));
    }

    @Override
    public int markUnusedAsUsedByUserIdAndType(UUID userId, VerificationTokenType type, Instant usedAt) {
        String sql = """
                UPDATE verification_tokens
                SET used_at = :usedAt
                WHERE user_id = :userId
                  AND type = :type
                  AND used_at IS NULL
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("usedAt", usedAt)
                .addValue("userId", userId)
                .addValue("type", type.name()));
    }
}
