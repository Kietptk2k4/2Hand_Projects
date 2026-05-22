package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ProductReviewRatingSummary;
import com.twohands.commerce_service.domain.review.ProductReviewSellerReply;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ShopReviewListItem;
import com.twohands.commerce_service.domain.review.ViewShopReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewShopReviewsResult;
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
import java.util.UUID;

@Repository
public class ViewShopReviewsRepositoryAdapter implements ViewShopReviewsRepository {

    private static final String SELLER_REVIEWS_FROM = """
            FROM reviews r
            INNER JOIN order_items oi ON oi.id = r.order_item_id
            WHERE r.seller_id = :sellerId
              AND r.status = :status
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewShopReviewsRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ViewShopReviewsResult findShopReviews(
            UUID sellerId,
            UUID shopId,
            Integer ratingFilter,
            ReviewStatus statusFilter,
            ProductReviewSort sort,
            PageQuery pageQuery
    ) {
        MapSqlParameterSource reviewParams = reviewParams(sellerId, statusFilter, ratingFilter);

        ProductReviewRatingSummary ratingSummary = loadRatingSummary(reviewParams);
        long totalItems = countReviews(reviewParams);
        List<ReviewRow> reviewRows = totalItems == 0
                ? List.of()
                : loadReviewPage(reviewParams, sort, pageQuery);

        List<UUID> reviewIds = reviewRows.stream().map(ReviewRow::reviewId).toList();
        Map<UUID, List<ReviewMediaItem>> mediaByReview = loadMediaByReviewIds(reviewIds);
        Map<UUID, ProductReviewSellerReply> repliesByReview = loadRepliesByReviewIds(reviewIds);

        List<ShopReviewListItem> reviews = reviewRows.stream()
                .map(row -> new ShopReviewListItem(
                        row.reviewId(),
                        row.orderItemId(),
                        row.productNameSnapshot(),
                        row.rating(),
                        row.comment(),
                        row.status(),
                        row.createdAt(),
                        mediaByReview.getOrDefault(row.reviewId(), List.of()),
                        repliesByReview.get(row.reviewId())
                ))
                .toList();

        return new ViewShopReviewsResult(
                shopId,
                ratingSummary,
                reviews,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    private ProductReviewRatingSummary loadRatingSummary(MapSqlParameterSource params) {
        String sql = """
                SELECT COALESCE(AVG(r.rating::numeric), 0) AS rating_avg,
                       COUNT(*)::int AS rating_count
                """
                + SELLER_REVIEWS_FROM
                + ratingFilterClause(params);
        return jdbcTemplate.query(sql, params, rs -> {
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

    private long countReviews(MapSqlParameterSource params) {
        String sql = "SELECT COUNT(*) " + SELLER_REVIEWS_FROM + ratingFilterClause(params);
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
                       r.order_item_id,
                       oi.product_name_snapshot,
                       r.rating,
                       r.comment,
                       r.status,
                       r.created_at
                """
                + SELLER_REVIEWS_FROM
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

    private MapSqlParameterSource reviewParams(UUID sellerId, ReviewStatus statusFilter, Integer ratingFilter) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("status", statusFilter.name());
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
                UUID.fromString(rs.getString("order_item_id")),
                rs.getString("product_name_snapshot"),
                rs.getInt("rating"),
                rs.getString("comment"),
                ReviewStatus.valueOf(rs.getString("status")),
                createdAt == null ? null : createdAt.toInstant()
        );
    }

    private record ReviewRow(
            UUID reviewId,
            UUID orderItemId,
            String productNameSnapshot,
            int rating,
            String comment,
            ReviewStatus status,
            Instant createdAt
    ) {
    }
}
