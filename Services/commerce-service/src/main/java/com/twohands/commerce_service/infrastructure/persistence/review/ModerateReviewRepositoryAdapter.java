package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.ModerateReviewRepository;
import com.twohands.commerce_service.domain.review.ReviewForModeration;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ModerateReviewRepositoryAdapter implements ModerateReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SellerRatingRecalculator sellerRatingRecalculator;

    public ModerateReviewRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            SellerRatingRecalculator sellerRatingRecalculator
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.sellerRatingRecalculator = sellerRatingRecalculator;
    }

    @Override
    public Optional<ReviewForModeration> findById(UUID reviewId) {
        String sql = """
                SELECT id, order_item_id, seller_id, buyer_id, rating, status::text AS status
                FROM reviews
                WHERE id = :reviewId
                """;
        List<ReviewForModeration> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reviewId", reviewId),
                this::mapReview
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean updateStatus(
            UUID reviewId,
            ReviewStatus currentStatus,
            ReviewStatus newStatus,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE reviews
                SET status = CAST(:newStatus AS review_status),
                    updated_at = :now
                WHERE id = :reviewId
                  AND status = CAST(:currentStatus AS review_status)
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("currentStatus", currentStatus.name())
                .addValue("newStatus", newStatus.name())
                .addValue("now", Timestamp.from(occurredAt)));
        return updated == 1;
    }

    @Override
    public SellerRatingSummary recalculateSellerRating(UUID sellerId, Instant occurredAt) {
        return sellerRatingRecalculator.recalculate(sellerId, occurredAt);
    }

    private ReviewForModeration mapReview(ResultSet rs, int rowNum) throws SQLException {
        return new ReviewForModeration(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getInt("rating"),
                ReviewStatus.valueOf(rs.getString("status"))
        );
    }
}
