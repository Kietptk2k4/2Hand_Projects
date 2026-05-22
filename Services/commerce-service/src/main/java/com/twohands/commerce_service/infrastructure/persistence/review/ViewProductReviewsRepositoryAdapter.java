package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ProductReviewListItem;
import com.twohands.commerce_service.domain.review.ProductReviewRatingSummary;
import com.twohands.commerce_service.domain.review.ProductReviewSellerReply;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.domain.review.ViewProductReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewProductReviewsResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewProductReviewsRepositoryAdapter implements ViewProductReviewsRepository {

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

    private static final String VISIBLE_REVIEWS_FROM = """
            FROM reviews r
            INNER JOIN order_items oi ON oi.id = r.order_item_id
            WHERE oi.product_id = :productId
              AND r.status = 'VISIBLE'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewProductReviewsRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewProductReviewsResult> findVisibleProductReviews(
            UUID productId,
            Integer ratingFilter,
            ProductReviewSort sort,
            PageQuery pageQuery,
            Instant now
    ) {
        if (!isProductBuyerVisible(productId, now)) {
            return Optional.empty();
        }

        ProductReviewRatingSummary ratingSummary = loadRatingSummary(productId);
        MapSqlParameterSource reviewParams = reviewParams(productId, ratingFilter);

        long totalItems = countVisibleReviews(reviewParams);
        List<ReviewRow> reviewRows = totalItems == 0
                ? List.of()
                : loadReviewPage(reviewParams, sort, pageQuery);

        List<UUID> reviewIds = reviewRows.stream().map(ReviewRow::reviewId).toList();
        Map<UUID, List<ReviewMediaItem>> mediaByReview = loadMediaByReviewIds(reviewIds);
        Map<UUID, ProductReviewSellerReply> repliesByReview = loadRepliesByReviewIds(reviewIds);

        List<ProductReviewListItem> reviews = reviewRows.stream()
                .map(row -> new ProductReviewListItem(
                        row.reviewId(),
                        row.rating(),
                        row.comment(),
                        row.createdAt(),
                        mediaByReview.getOrDefault(row.reviewId(), List.of()),
                        repliesByReview.get(row.reviewId())
                ))
                .toList();

        return Optional.of(new ViewProductReviewsResult(
                productId,
                ratingSummary,
                reviews,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        ));
    }

    private boolean isProductBuyerVisible(UUID productId, Instant now) {
        Boolean exists = jdbcTemplate.queryForObject(
                VISIBLE_PRODUCT_EXISTS,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("now", Timestamp.from(now)),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    private ProductReviewRatingSummary loadRatingSummary(UUID productId) {
        String sql = """
                SELECT COALESCE(AVG(r.rating::numeric), 0) AS rating_avg,
                       COUNT(*)::int AS rating_count
                FROM reviews r
                INNER JOIN order_items oi ON oi.id = r.order_item_id
                WHERE oi.product_id = :productId
                  AND r.status = 'VISIBLE'
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("productId", productId), rs -> {
            if (!rs.next()) {
                return new ProductReviewRatingSummary(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0);
            }
            BigDecimal ratingAvg = rs.getBigDecimal("rating_avg");
            return new ProductReviewRatingSummary(
                    ratingAvg == null
                            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            : ratingAvg.setScale(2, RoundingMode.HALF_UP),
                    rs.getInt("rating_count")
            );
        });
    }

    private long countVisibleReviews(MapSqlParameterSource params) {
        String sql = "SELECT COUNT(*) " + VISIBLE_REVIEWS_FROM + ratingFilterClause(params);
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count == null ? 0 : count;
    }

    private List<ReviewRow> loadReviewPage(
            MapSqlParameterSource params,
            ProductReviewSort sort,
            PageQuery pageQuery
    ) {
        String sql = """
                SELECT r.id AS review_id,
                       r.rating,
                       r.comment,
                       r.created_at
                """
                + VISIBLE_REVIEWS_FROM
                + ratingFilterClause(params)
                + " ORDER BY " + orderByClause(sort)
                + " LIMIT :limit OFFSET :offset";

        params.addValue("limit", pageQuery.limit());
        params.addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(sql, params, this::mapReviewRow);
    }

    private Map<UUID, List<ReviewMediaItem>> loadMediaByReviewIds(List<UUID> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        String sql = """
                SELECT id, review_id, url, type
                FROM review_media
                WHERE review_id IN (:reviewIds)
                ORDER BY review_id, id ASC
                """;
        Map<UUID, List<ReviewMediaItem>> result = new HashMap<>();
        jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reviewIds", reviewIds),
                (rs, rowNum) -> {
                    UUID reviewId = UUID.fromString(rs.getString("review_id"));
                    ReviewMediaItem item = new ReviewMediaItem(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("url"),
                            ReviewMediaType.valueOf(rs.getString("type"))
                    );
                    result.computeIfAbsent(reviewId, ignored -> new ArrayList<>()).add(item);
                    return item;
                }
        );
        return result;
    }

    private Map<UUID, ProductReviewSellerReply> loadRepliesByReviewIds(List<UUID> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        String sql = """
                SELECT id, review_id, content, created_at
                FROM review_replies
                WHERE review_id IN (:reviewIds)
                """;
        Map<UUID, ProductReviewSellerReply> result = new HashMap<>();
        jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reviewIds", reviewIds),
                (rs, rowNum) -> {
                    UUID reviewId = UUID.fromString(rs.getString("review_id"));
                    result.put(reviewId, new ProductReviewSellerReply(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                    return null;
                }
        );
        return result;
    }

    private MapSqlParameterSource reviewParams(UUID productId, Integer ratingFilter) {
        MapSqlParameterSource params = new MapSqlParameterSource("productId", productId);
        if (ratingFilter != null) {
            params.addValue("rating", ratingFilter);
        }
        return params;
    }

    private String ratingFilterClause(MapSqlParameterSource params) {
        return params.hasValue("rating") ? " AND r.rating = :rating" : "";
    }

    private String orderByClause(ProductReviewSort sort) {
        return switch (sort) {
            case OLDEST -> "r.created_at ASC";
            case RATING_DESC -> "r.rating DESC, r.created_at DESC";
            case RATING_ASC -> "r.rating ASC, r.created_at DESC";
            default -> "r.created_at DESC";
        };
    }

    private ReviewRow mapReviewRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new ReviewRow(
                UUID.fromString(rs.getString("review_id")),
                rs.getInt("rating"),
                rs.getString("comment"),
                createdAt == null ? null : createdAt.toInstant()
        );
    }

    private record ReviewRow(
            UUID reviewId,
            int rating,
            String comment,
            Instant createdAt
    ) {
    }
}
