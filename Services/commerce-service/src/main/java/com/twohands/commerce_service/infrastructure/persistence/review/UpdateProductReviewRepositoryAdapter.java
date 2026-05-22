package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import com.twohands.commerce_service.domain.review.UpdateProductReviewDraft;
import com.twohands.commerce_service.domain.review.UpdateProductReviewRepository;
import com.twohands.commerce_service.domain.review.UpdateProductReviewResult;
import com.twohands.commerce_service.domain.review.UpdateProductReviewSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UpdateProductReviewRepositoryAdapter implements UpdateProductReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SellerRatingRecalculator sellerRatingRecalculator;

    public UpdateProductReviewRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            SellerRatingRecalculator sellerRatingRecalculator
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.sellerRatingRecalculator = sellerRatingRecalculator;
    }

    @Override
    public Optional<UpdateProductReviewSnapshot> findByIdAndBuyerId(UUID reviewId, UUID buyerId) {
        String sql = """
                SELECT id, order_item_id, seller_id, buyer_id, rating, comment, status::text AS review_status
                FROM reviews
                WHERE id = :reviewId AND buyer_id = :buyerId
                """;
        List<UpdateProductReviewSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("reviewId", reviewId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> new UpdateProductReviewSnapshot(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("order_item_id")),
                        UUID.fromString(rs.getString("seller_id")),
                        UUID.fromString(rs.getString("buyer_id")),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        ReviewStatus.valueOf(rs.getString("review_status"))
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UpdateProductReviewResult updateReview(UpdateProductReviewDraft draft, Instant updatedAt) {
        String sql = """
                UPDATE reviews
                SET rating = :rating,
                    comment = :comment,
                    updated_at = :updatedAt
                WHERE id = :reviewId
                  AND buyer_id = :buyerId
                  AND status = CAST(:status AS review_status)
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("rating", draft.rating())
                .addValue("comment", draft.comment())
                .addValue("updatedAt", Timestamp.from(updatedAt))
                .addValue("reviewId", draft.reviewId())
                .addValue("buyerId", draft.buyerId())
                .addValue("status", ReviewStatus.VISIBLE.name()));

        if (updated == 0) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        return loadReview(draft.reviewId());
    }

    @Override
    public SellerRatingSummary recalculateSellerRating(UUID sellerId, Instant occurredAt) {
        return sellerRatingRecalculator.recalculate(sellerId, occurredAt);
    }

    @Override
    public SellerRatingSummary loadSellerRatingSummary(UUID sellerId) {
        return jdbcTemplate.query(
                """
                        SELECT COALESCE(rating_avg, 0) AS rating_avg,
                               COALESCE(rating_count, 0) AS rating_count
                        FROM seller_shops
                        WHERE seller_id = :sellerId
                        """,
                new MapSqlParameterSource("sellerId", sellerId),
                (rs, rowNum) -> new SellerRatingSummary(
                        rs.getBigDecimal("rating_avg").setScale(2, RoundingMode.HALF_UP),
                        rs.getInt("rating_count")
                )
        ).stream().findFirst().orElse(new SellerRatingSummary(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0));
    }

    private UpdateProductReviewResult loadReview(UUID reviewId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT id, order_item_id, seller_id, buyer_id, rating, comment,
                               status::text AS review_status, created_at, updated_at
                        FROM reviews
                        WHERE id = :reviewId
                        """,
                new MapSqlParameterSource("reviewId", reviewId),
                this::mapResult
        );
    }

    private UpdateProductReviewResult mapResult(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateProductReviewResult(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getInt("rating"),
                rs.getString("comment"),
                ReviewStatus.valueOf(rs.getString("review_status")),
                false,
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                null,
                0
        );
    }
}
