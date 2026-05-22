package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class SellerRatingRecalculator {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SellerRatingRecalculator(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SellerRatingSummary recalculate(UUID sellerId, Instant occurredAt) {
        String aggregateSql = """
                SELECT COALESCE(AVG(rating), 0) AS rating_avg,
                       COUNT(*) AS rating_count
                FROM reviews
                WHERE seller_id = :sellerId
                  AND status = 'VISIBLE'
                """;
        SellerRatingSummary summary = jdbcTemplate.query(
                aggregateSql,
                new MapSqlParameterSource("sellerId", sellerId),
                (rs, rowNum) -> new SellerRatingSummary(
                        rs.getBigDecimal("rating_avg").setScale(2, RoundingMode.HALF_UP),
                        rs.getInt("rating_count")
                )
        ).getFirst();

        String updateSql = """
                UPDATE seller_shops
                SET rating_avg = :ratingAvg,
                    rating_count = :ratingCount,
                    updated_at = :now
                WHERE seller_id = :sellerId
                """;
        jdbcTemplate.update(updateSql, new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("ratingAvg", summary.ratingAvg())
                .addValue("ratingCount", summary.ratingCount())
                .addValue("now", Timestamp.from(occurredAt)));

        return summary;
    }
}
