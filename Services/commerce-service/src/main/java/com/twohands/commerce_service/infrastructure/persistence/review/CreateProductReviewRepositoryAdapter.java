package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.CreateProductReviewDraft;
import com.twohands.commerce_service.domain.review.CreateProductReviewRepository;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ReviewableOrderItem;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
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
public class CreateProductReviewRepositoryAdapter implements CreateProductReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SellerRatingRecalculator sellerRatingRecalculator;

    public CreateProductReviewRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            SellerRatingRecalculator sellerRatingRecalculator
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.sellerRatingRecalculator = sellerRatingRecalculator;
    }

    @Override
    public Optional<ReviewableOrderItem> findReviewableOrderItem(UUID orderItemId, UUID buyerId) {
        String sql = """
                SELECT oi.id AS order_item_id,
                       oi.order_id,
                       oi.seller_id,
                       oi.product_id,
                       oi.status::text AS item_status,
                       o.buyer_id
                FROM order_items oi
                INNER JOIN orders o ON o.id = oi.order_id
                WHERE oi.id = :orderItemId
                  AND o.buyer_id = :buyerId
                """;
        List<ReviewableOrderItem> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("orderItemId", orderItemId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapReviewableOrderItem(rs)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean existsByOrderItemId(UUID orderItemId) {
        String sql = "SELECT COUNT(1) FROM reviews WHERE order_item_id = :orderItemId";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("orderItemId", orderItemId),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public CreateProductReviewResult createReview(CreateProductReviewDraft draft, Instant occurredAt) {
        UUID reviewId = UUID.randomUUID();
        try {
            insertReview(reviewId, draft, occurredAt);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS, "Review already exists for this order item", ex);
        }

        SellerRatingSummary ratingSummary = sellerRatingRecalculator.recalculate(draft.sellerId(), occurredAt);

        return new CreateProductReviewResult(
                reviewId,
                draft.orderItemId(),
                draft.sellerId(),
                draft.buyerId(),
                draft.rating(),
                draft.comment(),
                ReviewStatus.VISIBLE,
                occurredAt,
                ratingSummary.ratingAvg(),
                ratingSummary.ratingCount()
        );
    }

    private void insertReview(UUID reviewId, CreateProductReviewDraft draft, Instant occurredAt) {
        String sql = """
                INSERT INTO reviews(
                    id, order_item_id, seller_id, buyer_id, rating, comment,
                    status, created_at, updated_at
                ) VALUES (
                    :reviewId, :orderItemId, :sellerId, :buyerId, :rating, :comment,
                    CAST(:status AS review_status), :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("orderItemId", draft.orderItemId())
                .addValue("sellerId", draft.sellerId())
                .addValue("buyerId", draft.buyerId())
                .addValue("rating", draft.rating())
                .addValue("comment", draft.comment())
                .addValue("status", ReviewStatus.VISIBLE.name())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private ReviewableOrderItem mapReviewableOrderItem(ResultSet rs) throws SQLException {
        return new ReviewableOrderItem(
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("product_id")),
                rs.getString("item_status")
        );
    }
}
