package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.ReviewContextSnapshot;
import com.twohands.commerce_service.domain.review.ViewReviewContextRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewReviewContextRepositoryAdapter implements ViewReviewContextRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewReviewContextRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ReviewContextSnapshot> findByOrderItemIdAndBuyerId(UUID orderItemId, UUID buyerId) {
        String sql = """
                SELECT oi.id AS order_item_id,
                       oi.order_id,
                       oi.product_id,
                       oi.status::text AS item_status,
                       oi.product_name_snapshot,
                       oi.image_snapshot,
                       oi.shop_name_snapshot,
                       oi.final_price,
                       oi.completed_at,
                       r.id AS review_id
                FROM order_items oi
                INNER JOIN orders o ON o.id = oi.order_id
                LEFT JOIN reviews r ON r.order_item_id = oi.id
                WHERE oi.id = :orderItemId
                  AND o.buyer_id = :buyerId
                """;
        List<ReviewContextSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("orderItemId", orderItemId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapSnapshot(rs)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private ReviewContextSnapshot mapSnapshot(ResultSet rs) throws SQLException {
        String reviewIdRaw = rs.getString("review_id");
        UUID reviewId = reviewIdRaw != null ? UUID.fromString(reviewIdRaw) : null;
        return new ReviewContextSnapshot(
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("product_id")),
                rs.getString("item_status"),
                rs.getString("product_name_snapshot"),
                rs.getString("image_snapshot"),
                rs.getString("shop_name_snapshot"),
                rs.getBigDecimal("final_price"),
                optionalInstant(rs.getTimestamp("completed_at")),
                reviewId != null,
                reviewId
        );
    }

    private Instant optionalInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }
}
