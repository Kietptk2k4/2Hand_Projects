package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.domain.admin.AdminReviewDetailEntry;
import com.twohands.commerce_service.domain.admin.ViewReviewDetailForModerationRepository;
import com.twohands.commerce_service.domain.review.ReviewStatus;
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
public class ViewReviewDetailForModerationRepositoryAdapter implements ViewReviewDetailForModerationRepository {

    private static final String DETAIL_QUERY = """
            SELECT r.id AS review_id,
                   r.order_item_id,
                   oi.product_id,
                   oi.product_name_snapshot AS product_title,
                   oi.image_snapshot AS product_thumbnail_url,
                   r.buyer_id,
                   r.seller_id,
                   ss.id AS shop_id,
                   ss.shop_name,
                   r.rating,
                   r.comment,
                   r.status::text AS status,
                   r.created_at,
                   r.updated_at
            FROM reviews r
            INNER JOIN order_items oi ON oi.id = r.order_item_id
            INNER JOIN seller_shops ss ON ss.seller_id = r.seller_id
            WHERE r.id = :reviewId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewReviewDetailForModerationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<AdminReviewDetailEntry> findByReviewId(UUID reviewId) {
        MapSqlParameterSource params = new MapSqlParameterSource("reviewId", reviewId);
        List<AdminReviewDetailEntry> rows = jdbcTemplate.query(DETAIL_QUERY, params, this::mapEntry);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private AdminReviewDetailEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new AdminReviewDetailEntry(
                rs.getObject("review_id", UUID.class),
                rs.getObject("order_item_id", UUID.class),
                rs.getObject("product_id", UUID.class),
                rs.getString("product_title"),
                rs.getString("product_thumbnail_url"),
                rs.getObject("buyer_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getObject("shop_id", UUID.class),
                rs.getString("shop_name"),
                rs.getInt("rating"),
                rs.getString("comment"),
                ReviewStatus.valueOf(rs.getString("status")),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
