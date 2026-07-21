package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.PostImpressionRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class PostImpressionRepositoryAdapter implements PostImpressionRepository {

    private static final String INSERT = """
            INSERT INTO post_impression_log (
                user_id, post_id, shown_at, rank_position, model_version, model_name, request_id
            ) VALUES (
                :userId, :postId, :shownAt, :rankPosition, :modelVersion, :modelName, :requestId
            )
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostImpressionRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertImpressions(
            UUID userId,
            List<ImpressionRow> rows,
            Instant shownAt,
            Integer modelVersion,
            String modelName,
            String requestId
    ) {
        if (userId == null || rows == null || rows.isEmpty()) {
            return;
        }
        Instant effectiveShownAt = shownAt != null ? shownAt : Instant.now();
        MapSqlParameterSource[] batch = rows.stream()
                .filter(row -> row != null && row.postId() != null && !row.postId().isBlank())
                .map(row -> new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("postId", row.postId())
                        .addValue("shownAt", Timestamp.from(effectiveShownAt))
                        .addValue("rankPosition", row.rankPosition())
                        .addValue("modelVersion", modelVersion)
                        .addValue("modelName", modelName)
                        .addValue("requestId", requestId))
                .toArray(MapSqlParameterSource[]::new);
        if (batch.length == 0) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT, batch);
    }
}
