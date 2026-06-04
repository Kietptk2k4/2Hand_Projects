package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.infrastructure.persistence.JdbcSqlDialect;
import com.twohands.auth_service.infrastructure.persistence.JdbcTimestamps;
import com.twohands.auth_service.infrastructure.persistence.mapper.UserJdbcMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserJdbcMapper jdbcMapper;
    private final JdbcSqlDialect sqlDialect;

    public UserProfileRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            UserJdbcMapper jdbcMapper,
            JdbcSqlDialect sqlDialect
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcMapper = jdbcMapper;
        this.sqlDialect = sqlDialect;
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        String sql = """
                SELECT user_id, display_name, avatar_url, bio, website, %s as social_links,
                       is_private, created_at, updated_at
                FROM user_profiles
                WHERE user_id = :userId
                """.formatted(sqlDialect.jsonColumnAsText("social_links"));

        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId), (rs, rowNum) -> jdbcMapper.toUserProfile(rs))
                .stream()
                .findFirst();
    }

    @Override
    public UserProfile save(UserProfile profile) {
        String sql = """
                INSERT INTO user_profiles(user_id, display_name, avatar_url, bio, website, social_links, is_private, created_at, updated_at)
                VALUES (:userId, :displayName, :avatarUrl, :bio, :website, %s, :isPrivate, :createdAt, :updatedAt)
                """.formatted(sqlDialect.castJsonb("socialLinks"));

        String socialLinksJson = toSocialLinksJson(profile);

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", profile.userId())
                .addValue("displayName", profile.displayName())
                .addValue("avatarUrl", profile.avatarUrl())
                .addValue("bio", profile.bio())
                .addValue("website", profile.website())
                .addValue("socialLinks", socialLinksJson)
                .addValue("isPrivate", profile.isPrivate())
                .addValue("createdAt", JdbcTimestamps.from(profile.createdAt()))
                .addValue("updatedAt", JdbcTimestamps.from(profile.updatedAt())));

        return profile;
    }

    @Override
    public int updateByUserId(UserProfile profile) {
        String sql = """
                UPDATE user_profiles
                SET display_name = :displayName,
                    avatar_url = :avatarUrl,
                    bio = :bio,
                    website = :website,
                    social_links = %s,
                    is_private = :isPrivate,
                    updated_at = :updatedAt
                WHERE user_id = :userId
                """.formatted(sqlDialect.castJsonb("socialLinks"));

        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", profile.userId())
                .addValue("displayName", profile.displayName())
                .addValue("avatarUrl", profile.avatarUrl())
                .addValue("bio", profile.bio())
                .addValue("website", profile.website())
                .addValue("socialLinks", toSocialLinksJson(profile))
                .addValue("isPrivate", profile.isPrivate())
                .addValue("updatedAt", JdbcTimestamps.from(profile.updatedAt())));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbcTemplate.update("DELETE FROM user_profiles WHERE user_id = :userId", new MapSqlParameterSource("userId", userId));
    }

    private static String toSocialLinksJson(UserProfile profile) {
        if (profile.socialLinks() == null || profile.socialLinks().isEmpty()) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (var entry : profile.socialLinks().entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append('"').append(entry.getKey()).append('"')
                    .append(':')
                    .append('"').append(entry.getValue()).append('"');
            first = false;
        }
        builder.append('}');
        return builder.toString();
    }
}
