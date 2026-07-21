package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserSeenPostsRepositoryAdapter implements UserSeenPostsRepository {

    private static final String FIND_SEEN = """
            SELECT post_id
            FROM user_seen_posts
            WHERE user_id = :userId
            """;

    private static final String UPSERT = """
            INSERT INTO user_seen_posts (user_id, post_id, seen_at)
            VALUES (:userId, :postId, :seenAt)
            ON CONFLICT (user_id, post_id)
            DO UPDATE SET seen_at = EXCLUDED.seen_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserSeenPostsRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<String> findSeenPostIds(UUID userId) {
        if (userId == null) {
            return Set.of();
        }
        List<String> ids = jdbcTemplate.query(
                FIND_SEEN,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> rs.getString("post_id")
        );
        return new HashSet<>(ids);
    }

    @Override
    public void upsertSeenPosts(UUID userId, List<String> postIds, Instant seenAt) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return;
        }
        Instant effectiveSeenAt = seenAt != null ? seenAt : Instant.now();
        MapSqlParameterSource[] batch = postIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .map(postId -> new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("postId", postId)
                        .addValue("seenAt", Timestamp.from(effectiveSeenAt)))
                .toArray(MapSqlParameterSource[]::new);
        if (batch.length == 0) {
            return;
        }
        jdbcTemplate.batchUpdate(UPSERT, batch);
    }
}
