package com.twohands.social_service.infrastructure.persistence.jpa;

import com.twohands.social_service.domain.suggesteduser.SuggestedUsersRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class SuggestedUsersJdbcRepository implements SuggestedUsersRepository {

    private static final String MUTUAL_COUNTS = """
            SELECT f2.followee_id AS user_id, COUNT(*) AS mutual_count
            FROM follows f1
            INNER JOIN follows f2
                ON f1.followee_id = f2.follower_id
               AND f2.status = CAST('ACCEPTED' AS follow_status)
            WHERE f1.follower_id = :viewerId
              AND f1.status = CAST('ACCEPTED' AS follow_status)
              AND f2.followee_id IN (:candidateIds)
            GROUP BY f2.followee_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SuggestedUsersJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<UUID, Long> findMutualFollowCounts(UUID viewerId, Collection<UUID> candidateUserIds) {
        if (candidateUserIds == null || candidateUserIds.isEmpty()) {
            return Map.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("viewerId", viewerId)
                .addValue("candidateIds", candidateUserIds);

        Map<UUID, Long> counts = new HashMap<>();
        jdbcTemplate.query(
                MUTUAL_COUNTS,
                params,
                (rs, rowNum) -> counts.put(
                        UUID.fromString(rs.getString("user_id")),
                        rs.getLong("mutual_count")
                )
        );
        return counts;
    }
}