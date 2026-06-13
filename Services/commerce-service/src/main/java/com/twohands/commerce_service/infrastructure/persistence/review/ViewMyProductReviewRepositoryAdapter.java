package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.MyProductReviewSnapshot;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ViewMyProductReviewRepository;
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
public class ViewMyProductReviewRepositoryAdapter implements ViewMyProductReviewRepository {

    private static final String VISIBLE_PRODUCT_EXISTS = """
            SELECT EXISTS (
                SELECT 1
                FROM products p
                INNER JOIN seller_shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
                INNER JOIN product_categories pc ON pc.id = p.category_id AND pc.is_active = TRUE
                INNER JOIN LATERAL (
                    SELECT 1
                    FROM product_prices pp
                    WHERE pp.product_id = p.id
                      AND pp.start_at <= :now
                      AND (pp.end_at IS NULL OR pp.end_at > :now)
                    ORDER BY pp.start_at DESC
                    LIMIT 1
                ) active_price ON TRUE
                WHERE p.id = :productId
                  AND p.status IN ('ACTIVE', 'OUT_OF_STOCK')
            )
            """;

    private static final String BUYER_REVIEW_SQL = """
            SELECT r.id AS review_id,
                   r.order_item_id,
                   r.rating,
                   r.comment,
                   r.status::text AS status,
                   r.created_at,
                   r.updated_at
            FROM reviews r
            INNER JOIN order_items oi ON oi.id = r.order_item_id
            WHERE oi.product_id = :productId
              AND r.buyer_id = :buyerId
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewMyProductReviewRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isProductBuyerVisible(UUID productId, Instant now) {
        Boolean exists = jdbcTemplate.queryForObject(
                VISIBLE_PRODUCT_EXISTS,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("now", Timestamp.from(now)),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Optional<MyProductReviewSnapshot> findBuyerReviewForProduct(UUID buyerId, UUID productId) {
        List<MyProductReviewSnapshot> rows = jdbcTemplate.query(
                BUYER_REVIEW_SQL,
                new MapSqlParameterSource()
                        .addValue("buyerId", buyerId)
                        .addValue("productId", productId),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private MyProductReviewSnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        ReviewStatus status = ReviewStatus.valueOf(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new MyProductReviewSnapshot(
                true,
                null,
                UUID.fromString(rs.getString("review_id")),
                UUID.fromString(rs.getString("order_item_id")),
                rs.getInt("rating"),
                rs.getString("comment"),
                status,
                createdAt == null ? null : createdAt.toInstant(),
                updatedAt == null ? null : updatedAt.toInstant(),
                status == ReviewStatus.VISIBLE
        );
    }
}
