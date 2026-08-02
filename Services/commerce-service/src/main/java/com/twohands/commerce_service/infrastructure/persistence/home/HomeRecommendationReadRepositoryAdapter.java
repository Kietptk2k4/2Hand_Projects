package com.twohands.commerce_service.infrastructure.persistence.home;

import com.twohands.commerce_service.domain.home.HomeProductSnapshot;
import com.twohands.commerce_service.domain.home.HomeRecommendationReadRepository;
import com.twohands.commerce_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class HomeRecommendationReadRepositoryAdapter implements HomeRecommendationReadRepository {

    private static final String PRODUCT_SELECT = """
            SELECT p.id AS product_id,
                   p.seller_id,
                   p.shop_id,
                   s.shop_name,
                   p.category_id,
                   p.brand_id,
                   p.title,
                   p.created_at,
                   thumbnail.media_url AS thumbnail_url,
                   active_price.price AS list_price,
                   active_price.sale_price,
                   COALESCE(active_price.sale_price, active_price.price) AS effective_price,
                   COALESCE(pi.stock_quantity, 0) AS stock_quantity,
                   COALESCE(ss.is_vacation, FALSE) AS shop_vacation,
                   review_summary.rating_avg,
                   review_summary.rating_count,
                   COALESCE(pop_all.completed_count, 0) AS popularity_raw
            """;

    private static final String PRODUCT_FROM = """
            FROM products p
            INNER JOIN seller_shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
            INNER JOIN product_categories pc ON pc.id = p.category_id AND pc.is_active = TRUE
            INNER JOIN LATERAL (
                SELECT price, sale_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :asOf
                  AND (pp.end_at IS NULL OR pp.end_at > :asOf)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN product_inventories pi ON pi.product_id = p.id
            LEFT JOIN shop_settings ss ON ss.shop_id = p.shop_id
            LEFT JOIN LATERAL (
                SELECT pm.media_url
                FROM product_media pm
                WHERE pm.product_id = p.id
                  AND pm.media_type = 'IMAGE'
                ORDER BY pm.sort_order ASC, pm.created_at ASC
                LIMIT 1
            ) thumbnail ON TRUE
            LEFT JOIN LATERAL (
                SELECT COALESCE(AVG(r.rating::numeric), 0) AS rating_avg,
                       COUNT(*)::int AS rating_count
                FROM reviews r
                INNER JOIN order_items oi ON oi.id = r.order_item_id
                WHERE oi.product_id = p.id
                  AND r.status = 'VISIBLE'
            ) review_summary ON TRUE
            LEFT JOIN LATERAL (
                SELECT COUNT(*)::bigint AS completed_count
                FROM order_items oi
                WHERE oi.product_id = p.id
                  AND oi.status = 'COMPLETED'
                  AND oi.completed_at < :asOf
            ) pop_all ON TRUE
            WHERE p.status = 'ACTIVE'
              AND COALESCE(pi.stock_quantity, 0) > 0
              AND COALESCE(ss.is_vacation, FALSE) = FALSE
              AND p.seller_id <> :excludedSellerId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public HomeRecommendationReadRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CommerceInteraction> findCompletedInteractions(UUID userId, Instant since, Instant asOf) {
        return jdbcTemplate.query(
                """
                        SELECT p.category_id,
                               p.brand_id,
                               p.shop_id,
                               oi.unit_price_snapshot,
                               oi.completed_at
                        FROM orders o
                        INNER JOIN order_items oi ON oi.order_id = o.id
                        INNER JOIN products p ON p.id = oi.product_id
                        WHERE o.buyer_id = :userId
                          AND oi.status = 'COMPLETED'
                          AND oi.completed_at >= :since
                          AND oi.completed_at < :asOf
                        """,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("since", JdbcTimestamps.from(since))
                        .addValue("asOf", JdbcTimestamps.from(asOf)),
                (rs, rowNum) -> new CommerceInteraction(
                        uuidOrNull(rs.getString("category_id")),
                        uuidOrNull(rs.getString("brand_id")),
                        uuidOrNull(rs.getString("shop_id")),
                        rs.getBigDecimal("unit_price_snapshot"),
                        rs.getTimestamp("completed_at").toInstant(),
                        1.0
                )
        );
    }

    @Override
    public List<CommerceInteraction> findCartInteractions(UUID userId, Instant since, Instant asOf) {
        return jdbcTemplate.query(
                """
                        SELECT p.category_id,
                               p.brand_id,
                               p.shop_id,
                               COALESCE(active_price.sale_price, active_price.price) AS effective_price,
                               ci.created_at
                        FROM carts c
                        INNER JOIN cart_items ci ON ci.cart_id = c.id
                        INNER JOIN products p ON p.id = ci.product_id
                        INNER JOIN LATERAL (
                            SELECT price, sale_price
                            FROM product_prices pp
                            WHERE pp.product_id = p.id
                              AND pp.start_at <= :asOf
                              AND (pp.end_at IS NULL OR pp.end_at > :asOf)
                            ORDER BY pp.start_at DESC
                            LIMIT 1
                        ) active_price ON TRUE
                        WHERE c.user_id = :userId
                          AND ci.status <> 'REMOVED'
                          AND ci.created_at >= :since
                          AND ci.created_at < :asOf
                        """,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("since", JdbcTimestamps.from(since))
                        .addValue("asOf", JdbcTimestamps.from(asOf)),
                (rs, rowNum) -> new CommerceInteraction(
                        uuidOrNull(rs.getString("category_id")),
                        uuidOrNull(rs.getString("brand_id")),
                        uuidOrNull(rs.getString("shop_id")),
                        rs.getBigDecimal("effective_price"),
                        rs.getTimestamp("created_at").toInstant(),
                        0.6
                )
        );
    }

    @Override
    public List<SocialTagSignal> findSocialSignals(UUID userId) {
        return jdbcTemplate.query(
                """
                        SELECT tag_type, tag, score, computed_at, as_of
                        FROM user_social_interest_export
                        WHERE user_id = :userId
                        """,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> new SocialTagSignal(
                        rs.getString("tag_type"),
                        rs.getString("tag"),
                        rs.getDouble("score"),
                        rs.getTimestamp("computed_at").toInstant(),
                        rs.getTimestamp("as_of").toInstant()
                )
        );
    }

    @Override
    public List<HomeProductSnapshot> findNewestProducts(UUID excludedSellerId, Instant asOf, int limit) {
        return queryProducts(
                PRODUCT_SELECT + PRODUCT_FROM + " ORDER BY p.created_at DESC, p.id ASC LIMIT :limit",
                baseProductParams(excludedSellerId, asOf).addValue("limit", limit)
        );
    }

    @Override
    public List<HomeProductSnapshot> findPopularProducts90d(UUID excludedSellerId, Instant since, Instant asOf, int limit) {
        String sql = PRODUCT_SELECT
                + """
                , COALESCE(pop90.completed_90d, 0) AS completed_90d
                """
                + """
                FROM products p
                INNER JOIN seller_shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
                INNER JOIN product_categories pc ON pc.id = p.category_id AND pc.is_active = TRUE
                INNER JOIN LATERAL (
                    SELECT price, sale_price
                    FROM product_prices pp
                    WHERE pp.product_id = p.id
                      AND pp.start_at <= :asOf
                      AND (pp.end_at IS NULL OR pp.end_at > :asOf)
                    ORDER BY pp.start_at DESC
                    LIMIT 1
                ) active_price ON TRUE
                LEFT JOIN product_inventories pi ON pi.product_id = p.id
                LEFT JOIN shop_settings ss ON ss.shop_id = p.shop_id
                LEFT JOIN LATERAL (
                    SELECT pm.media_url
                    FROM product_media pm
                    WHERE pm.product_id = p.id
                      AND pm.media_type = 'IMAGE'
                    ORDER BY pm.sort_order ASC, pm.created_at ASC
                    LIMIT 1
                ) thumbnail ON TRUE
                LEFT JOIN LATERAL (
                    SELECT COALESCE(AVG(r.rating::numeric), 0) AS rating_avg,
                           COUNT(*)::int AS rating_count
                    FROM reviews r
                    INNER JOIN order_items oi ON oi.id = r.order_item_id
                    WHERE oi.product_id = p.id
                      AND r.status = 'VISIBLE'
                ) review_summary ON TRUE
                LEFT JOIN LATERAL (
                    SELECT COUNT(*)::bigint AS completed_count
                    FROM order_items oi
                    WHERE oi.product_id = p.id
                      AND oi.status = 'COMPLETED'
                      AND oi.completed_at < :asOf
                ) pop_all ON TRUE
                LEFT JOIN LATERAL (
                    SELECT COUNT(*)::bigint AS completed_90d
                    FROM order_items oi
                    WHERE oi.product_id = p.id
                      AND oi.status = 'COMPLETED'
                      AND oi.completed_at >= :since
                      AND oi.completed_at < :asOf
                ) pop90 ON TRUE
                WHERE p.status = 'ACTIVE'
                  AND COALESCE(pi.stock_quantity, 0) > 0
                  AND COALESCE(ss.is_vacation, FALSE) = FALSE
                  AND p.seller_id <> :excludedSellerId
                ORDER BY completed_90d DESC, p.created_at DESC, p.id ASC
                LIMIT :limit
                """;
        return queryProducts(sql, baseProductParams(excludedSellerId, asOf)
                .addValue("since", JdbcTimestamps.from(since))
                .addValue("limit", limit));
    }

    @Override
    public List<HomeProductSnapshot> findTopRatedProducts(UUID excludedSellerId, Instant asOf, int limit) {
        return queryProducts(
                PRODUCT_SELECT
                        + PRODUCT_FROM
                        + " AND review_summary.rating_count >= 3 ORDER BY review_summary.rating_avg DESC, p.created_at DESC, p.id ASC LIMIT :limit",
                baseProductParams(excludedSellerId, asOf).addValue("limit", limit)
        );
    }

    @Override
    public List<HomeProductSnapshot> findProductsByCategories(
            Collection<UUID> categoryIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return queryProducts(
                PRODUCT_SELECT + PRODUCT_FROM + " AND p.category_id IN (:ids) ORDER BY p.created_at DESC, p.id ASC LIMIT :limit",
                baseProductParams(excludedSellerId, asOf)
                        .addValue("ids", categoryIds)
                        .addValue("limit", limit)
        );
    }

    @Override
    public List<HomeProductSnapshot> findProductsByBrands(
            Collection<UUID> brandIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    ) {
        if (brandIds == null || brandIds.isEmpty()) {
            return List.of();
        }
        return queryProducts(
                PRODUCT_SELECT + PRODUCT_FROM + " AND p.brand_id IN (:ids) ORDER BY p.created_at DESC, p.id ASC LIMIT :limit",
                baseProductParams(excludedSellerId, asOf)
                        .addValue("ids", brandIds)
                        .addValue("limit", limit)
        );
    }

    @Override
    public List<HomeProductSnapshot> findProductsByShops(
            Collection<UUID> shopIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    ) {
        if (shopIds == null || shopIds.isEmpty()) {
            return List.of();
        }
        return queryProducts(
                PRODUCT_SELECT + PRODUCT_FROM + " AND p.shop_id IN (:ids) ORDER BY p.created_at DESC, p.id ASC LIMIT :limit",
                baseProductParams(excludedSellerId, asOf)
                        .addValue("ids", shopIds)
                        .addValue("limit", limit)
        );
    }

    @Override
    public List<EntityNeighbor> findCategoryNeighbors(Collection<UUID> categoryIds, int limitPerSeed) {
        return findNeighbors("LEAF_CATEGORY", categoryIds, limitPerSeed);
    }

    @Override
    public List<EntityNeighbor> findBrandNeighbors(Collection<UUID> brandIds, int limitPerSeed) {
        return findNeighbors("BRAND", brandIds, limitPerSeed);
    }

    @Override
    public List<AssociationRule> findAssociationRules(String tagType, Collection<String> tags, double minConfidence) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query(
                """
                        SELECT tag, category_id, confidence
                        FROM social_tag_category_ar
                        WHERE tag_type = :tagType
                          AND tag IN (:tags)
                          AND confidence >= :minConfidence
                        ORDER BY confidence DESC, category_id ASC
                        """,
                new MapSqlParameterSource()
                        .addValue("tagType", tagType)
                        .addValue("tags", tags)
                        .addValue("minConfidence", minConfidence),
                (rs, rowNum) -> new AssociationRule(
                        rs.getString("tag"),
                        UUID.fromString(rs.getString("category_id")),
                        rs.getDouble("confidence")
                )
        );
    }

    private List<EntityNeighbor> findNeighbors(String entityType, Collection<UUID> entityIds, int limitPerSeed) {
        if (entityIds == null || entityIds.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query(
                """
                        SELECT entity_id, neighbor_id, score
                        FROM (
                            SELECT entity_id,
                                   neighbor_id,
                                   score,
                                   ROW_NUMBER() OVER (PARTITION BY entity_id ORDER BY score DESC, neighbor_id ASC) AS rn
                            FROM entity_cooccur
                            WHERE entity_type = :entityType
                              AND neighbor_type = :entityType
                              AND entity_id IN (:entityIds)
                        ) ranked
                        WHERE rn <= :limitPerSeed
                        ORDER BY entity_id ASC, score DESC, neighbor_id ASC
                        """,
                new MapSqlParameterSource()
                        .addValue("entityType", entityType)
                        .addValue("entityIds", entityIds)
                        .addValue("limitPerSeed", limitPerSeed),
                (rs, rowNum) -> new EntityNeighbor(
                        UUID.fromString(rs.getString("entity_id")),
                        UUID.fromString(rs.getString("neighbor_id")),
                        rs.getDouble("score")
                )
        );
    }

    private List<HomeProductSnapshot> queryProducts(String sql, MapSqlParameterSource params) {
        return jdbcTemplate.query(sql, params, this::mapProduct);
    }

    private MapSqlParameterSource baseProductParams(UUID excludedSellerId, Instant asOf) {
        return new MapSqlParameterSource()
                .addValue("excludedSellerId", excludedSellerId)
                .addValue("asOf", JdbcTimestamps.from(asOf));
    }

    private HomeProductSnapshot mapProduct(ResultSet rs, int rowNum) throws SQLException {
        BigDecimal ratingAvg = rs.getBigDecimal("rating_avg");
        return new HomeProductSnapshot(
                UUID.fromString(rs.getString("product_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("shop_name"),
                UUID.fromString(rs.getString("category_id")),
                uuidOrNull(rs.getString("brand_id")),
                rs.getString("title"),
                rs.getString("thumbnail_url"),
                rs.getBigDecimal("list_price"),
                rs.getBigDecimal("sale_price"),
                rs.getBigDecimal("effective_price"),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant(),
                ratingAvg == null ? null : ratingAvg.setScale(2, RoundingMode.HALF_UP),
                rs.getInt("rating_count"),
                rs.getLong("popularity_raw"),
                rs.getInt("stock_quantity") > 0,
                rs.getBoolean("shop_vacation")
        );
    }

    private static UUID uuidOrNull(String raw) {
        return raw == null ? null : UUID.fromString(raw);
    }
}
