package com.twohands.commerce_service.infrastructure.persistence.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.home.HomeInteractionRepository;
import com.twohands.commerce_service.domain.home.RankingMode;
import com.twohands.commerce_service.domain.home.RetrievalSource;
import com.twohands.commerce_service.infrastructure.persistence.JdbcTimestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Repository
public class HomeInteractionRepositoryAdapter implements HomeInteractionRepository {

    private static final Logger log = LoggerFactory.getLogger(HomeInteractionRepositoryAdapter.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public HomeInteractionRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Async
    public void saveImpressions(
            UUID userId,
            Instant shownAt,
            String requestId,
            RankingMode rankingMode,
            String modelName,
            Integer modelVersion,
            List<ServedCandidate> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        try {
            String sql = """
                    INSERT INTO home_impression_log (
                        user_id, product_id, shown_at, rank_position, model_name, model_version,
                        request_id, ranking_mode, sources, personal_score, cf_score, ar_score
                    ) VALUES (
                        :userId, :productId, :shownAt, :rankPosition, :modelName, :modelVersion,
                        :requestId, :rankingMode, CAST(:sources AS jsonb), :personalScore, :cfScore, :arScore
                    )
                    """;
            MapSqlParameterSource[] batch = new MapSqlParameterSource[candidates.size()];
            for (int i = 0; i < candidates.size(); i++) {
                ServedCandidate servedCandidate = candidates.get(i);
                batch[i] = new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("productId", servedCandidate.candidate().product().productId())
                        .addValue("shownAt", JdbcTimestamps.from(shownAt))
                        .addValue("rankPosition", servedCandidate.rankPosition())
                        .addValue("modelName", rankingMode == RankingMode.LIGHTGBM ? modelName : null)
                        .addValue("modelVersion", rankingMode == RankingMode.LIGHTGBM ? modelVersion : null)
                        .addValue("requestId", requestId)
                        .addValue("rankingMode", rankingMode.name())
                        .addValue("sources", serializeSources(servedCandidate.candidate().sources()))
                        .addValue("personalScore", servedCandidate.candidate().personalScore())
                        .addValue("cfScore", servedCandidate.candidate().cfScore())
                        .addValue("arScore", servedCandidate.candidate().arScore());
            }
            jdbcTemplate.batchUpdate(sql, batch);
        } catch (Exception ex) {
            log.warn("Failed to save Commerce Home impressions for request {}", requestId, ex);
        }
    }

    @Override
    @Async
    public void saveClick(UUID userId, UUID productId, Instant occurredAt, String requestId) {
        try {
            jdbcTemplate.update(
                    """
                            INSERT INTO home_engage_event (user_id, product_id, event_type, occurred_at, request_id)
                            VALUES (:userId, :productId, 'CLICK', :occurredAt, :requestId)
                            """,
                    new MapSqlParameterSource()
                            .addValue("userId", userId)
                            .addValue("productId", productId)
                            .addValue("occurredAt", JdbcTimestamps.from(occurredAt))
                            .addValue("requestId", requestId)
            );
        } catch (Exception ex) {
            log.warn("Failed to save Commerce Home click for product {}", productId, ex);
        }
    }

    private String serializeSources(java.util.EnumSet<RetrievalSource> sources) throws Exception {
        List<String> values = new ArrayList<>(sources.stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList());
        return objectMapper.writeValueAsString(values);
    }
}
