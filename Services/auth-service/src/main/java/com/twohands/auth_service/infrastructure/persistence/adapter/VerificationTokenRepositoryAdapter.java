package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.infrastructure.persistence.JdbcPgEnumTypes;
import com.twohands.auth_service.infrastructure.persistence.JdbcSqlDialect;
import com.twohands.auth_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcSqlDialect sqlDialect;

    public VerificationTokenRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate, JdbcSqlDialect sqlDialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlDialect = sqlDialect;
    }

    @Override
    public Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationTokenType type) {
        String sql = """
                SELECT id, user_id, token_hash, type, expires_at, used_at, created_at
                FROM verification_tokens
                WHERE token_hash = :tokenHash AND type = %s
                """.formatted(sqlDialect.castEnum("type", JdbcPgEnumTypes.VERIFICATION_TOKEN_TYPE));

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
    public List<VerificationToken> findUnusedByType(VerificationTokenType type, Instant now) {
        String sql = """
                SELECT id, user_id, token_hash, type, expires_at, used_at, created_at
                FROM verification_tokens
                WHERE type = %s
                  AND used_at IS NULL
                  AND expires_at > :now
                """.formatted(sqlDialect.castEnum("type", JdbcPgEnumTypes.VERIFICATION_TOKEN_TYPE));

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource()
                        .addValue("type", type.name())
                        .addValue("now", JdbcTimestamps.from(now)),
                (rs, rowNum) -> new VerificationToken(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("token_hash"),
                        VerificationTokenType.valueOf(rs.getString("type")),
                        rs.getTimestamp("expires_at").toInstant(),
                        null,
                        rs.getTimestamp("created_at").toInstant()
                ));
    }

    @Override
    public List<VerificationToken> findUsedByType(VerificationTokenType type) {
        String sql = """
                SELECT id, user_id, token_hash, type, expires_at, used_at, created_at
                FROM verification_tokens
                WHERE type = %s
                  AND used_at IS NOT NULL
                ORDER BY used_at DESC
                LIMIT 200
                """.formatted(sqlDialect.castEnum("type", JdbcPgEnumTypes.VERIFICATION_TOKEN_TYPE));

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("type", type.name()),
                (rs, rowNum) -> new VerificationToken(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("token_hash"),
                        VerificationTokenType.valueOf(rs.getString("type")),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("used_at").toInstant(),
                        rs.getTimestamp("created_at").toInstant()
                ));
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        String sql = """
                INSERT INTO verification_tokens(id, user_id, token_hash, type, expires_at, used_at, created_at)
                VALUES (:id, :userId, :tokenHash, %s, :expiresAt, :usedAt, :createdAt)
                """.formatted(sqlDialect.castEnum("type", JdbcPgEnumTypes.VERIFICATION_TOKEN_TYPE));

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", token.id())
                .addValue("userId", token.userId())
                .addValue("tokenHash", token.tokenHash())
                .addValue("type", token.type().name())
                .addValue("expiresAt", JdbcTimestamps.from(token.expiresAt()))
                .addValue("usedAt", JdbcTimestamps.from(token.usedAt()))
                .addValue("createdAt", JdbcTimestamps.from(token.createdAt())));

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
                  AND type = %s
                  AND used_at IS NULL
                """.formatted(sqlDialect.castEnum("type", JdbcPgEnumTypes.VERIFICATION_TOKEN_TYPE));
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("usedAt", JdbcTimestamps.from(usedAt))
                .addValue("userId", userId)
                .addValue("type", type.name()));
    }

    @Override
    public int markUsedById(UUID tokenId, Instant usedAt) {
        String sql = """
                UPDATE verification_tokens
                SET used_at = :usedAt
                WHERE id = :id
                  AND used_at IS NULL
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("usedAt", JdbcTimestamps.from(usedAt))
                .addValue("id", tokenId));
    }
}
