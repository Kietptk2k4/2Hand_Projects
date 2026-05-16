package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.oauth.OAuthAccount;
import com.twohands.auth_service.domain.oauth.OAuthAccountRepository;
import com.twohands.auth_service.domain.oauth.OAuthProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OAuthAccountRepositoryAdapter implements OAuthAccountRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OAuthAccountRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        String sql = """
                SELECT id, user_id, provider, provider_user_id, email, created_at, updated_at
                FROM oauth_accounts
                WHERE provider = :provider AND provider_user_id = :providerUserId
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                        .addValue("provider", provider.name())
                        .addValue("providerUserId", providerUserId), (rs, rowNum) -> map(rs))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<OAuthAccount> findByUserIdAndProvider(UUID userId, OAuthProvider provider) {
        String sql = """
                SELECT id, user_id, provider, provider_user_id, email, created_at, updated_at
                FROM oauth_accounts
                WHERE user_id = :userId AND provider = :provider
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("provider", provider.name()), (rs, rowNum) -> map(rs))
                .stream()
                .findFirst();
    }

    @Override
    public OAuthAccount save(OAuthAccount account) {
        String sql = """
                INSERT INTO oauth_accounts(id, user_id, provider, provider_user_id, email, created_at, updated_at)
                VALUES (:id, :userId, :provider, :providerUserId, :email, :createdAt, :updatedAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", account.id())
                .addValue("userId", account.userId())
                .addValue("provider", account.provider().name())
                .addValue("providerUserId", account.providerUserId())
                .addValue("email", account.email())
                .addValue("createdAt", account.createdAt())
                .addValue("updatedAt", account.updatedAt()));
        return account;
    }

    private OAuthAccount map(ResultSet rs) throws SQLException {
        return new OAuthAccount(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                OAuthProvider.valueOf(rs.getString("provider")),
                rs.getString("provider_user_id"),
                rs.getString("email"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
