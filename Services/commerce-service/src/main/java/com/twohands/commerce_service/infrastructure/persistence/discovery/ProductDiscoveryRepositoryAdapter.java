package com.twohands.commerce_service.infrastructure.persistence.discovery;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.product.ProductStatus;
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
import java.util.UUID;

@Repository
public class ProductDiscoveryRepositoryAdapter implements ProductDiscoveryRepository {

    private static final String VISIBLE_PRODUCT_BASE = """
            FROM products p
            INNER JOIN seller_shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
            INNER JOIN product_categories pc ON pc.id = p.category_id AND pc.is_active = TRUE
            INNER JOIN LATERAL (
                SELECT price, sale_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :now
                  AND (pp.end_at IS NULL OR pp.end_at > :now)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN product_inventories pi ON pi.product_id = p.id
            LEFT JOIN shop_settings ss ON ss.shop_id = s.id
            LEFT JOIN LATERAL (
                SELECT pm.media_url
                FROM product_media pm
                WHERE pm.product_id = p.id
                ORDER BY pm.sort_order ASC, pm.created_at ASC
                LIMIT 1
            ) thumbnail ON TRUE
            WHERE p.status IN ('ACTIVE', 'OUT_OF_STOCK')
              AND p.category_id IN (:categoryIds)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductDiscoveryRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countVisibleProductsByCategories(List<UUID> categoryIds, Instant now) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return 0;
        }
        String sql = "SELECT COUNT(DISTINCT p.id) " + VISIBLE_PRODUCT_BASE;
        Long count = jdbcTemplate.queryForObject(
                sql,
                baseParams(categoryIds, now),
                Long.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public List<ProductCardSummary> findVisibleProductsByCategories(
            List<UUID> categoryIds,
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT p.id AS product_id,
                       p.title,
                       thumbnail.media_url AS thumbnail_url,
                       p.shop_id,
                       s.shop_name,
                       p.category_id,
                       p.condition,
                       p.status::text AS product_status,
                       active_price.price,
                       active_price.sale_price,
                       COALESCE(active_price.sale_price, active_price.price) AS effective_price,
                       COALESCE(pi.stock_quantity, 0) AS stock_quantity,
                       COALESCE(pi.low_stock_threshold, 0) AS low_stock_threshold,
                       s.rating_avg,
                       s.rating_count,
                       COALESCE(ss.is_vacation, FALSE) AS shop_vacation,
                       ss.vacation_message
                """
                + VISIBLE_PRODUCT_BASE
                + " ORDER BY " + orderByClause(sort)
                + " LIMIT :limit OFFSET :offset";

        MapSqlParameterSource params = baseParams(categoryIds, now)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(sql, params, this::mapProductCard);
    }

    private MapSqlParameterSource baseParams(List<UUID> categoryIds, Instant now) {
        return new MapSqlParameterSource()
                .addValue("categoryIds", categoryIds)
                .addValue("now", Timestamp.from(now));
    }

    private String orderByClause(ProductDiscoverySort sort) {
        return switch (sort) {
            case PRICE_ASC -> "effective_price ASC, p.created_at DESC";
            case PRICE_DESC -> "effective_price DESC, p.created_at DESC";
            default -> "p.created_at DESC";
        };
    }

    private ProductCardSummary mapProductCard(ResultSet rs, int rowNum) throws SQLException {
        int stockQuantity = rs.getInt("stock_quantity");
        int lowStockThreshold = rs.getInt("low_stock_threshold");
        boolean inStock = stockQuantity > 0;
        boolean lowStock = inStock && stockQuantity <= lowStockThreshold;

        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal salePrice = rs.getBigDecimal("sale_price");
        BigDecimal effectivePrice = rs.getBigDecimal("effective_price");

        return new ProductCardSummary(
                UUID.fromString(rs.getString("product_id")),
                rs.getString("title"),
                rs.getString("thumbnail_url"),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("shop_name"),
                UUID.fromString(rs.getString("category_id")),
                rs.getString("condition"),
                ProductStatus.valueOf(rs.getString("product_status")),
                price,
                salePrice,
                effectivePrice,
                inStock,
                lowStock,
                rs.getBigDecimal("rating_avg").setScale(2, RoundingMode.HALF_UP),
                rs.getInt("rating_count"),
                rs.getBoolean("shop_vacation"),
                rs.getString("vacation_message")
        );
    }
}
