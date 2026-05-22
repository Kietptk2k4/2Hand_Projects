package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.ViewProductDetailAttributeItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailCategory;
import com.twohands.commerce_service.domain.product.ViewProductDetailInventorySummary;
import com.twohands.commerce_service.domain.product.ViewProductDetailMediaItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailRepository;
import com.twohands.commerce_service.domain.product.ViewProductDetailResult;
import com.twohands.commerce_service.domain.product.ViewProductDetailShop;
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
public class ViewProductDetailRepositoryAdapter implements ViewProductDetailRepository {

    private static final String HEADER_SQL = """
            SELECT p.id AS product_id,
                   p.title,
                   p.description,
                   p.condition,
                   p.weight_gram,
                   p.status::text AS product_status,
                   pc.id AS category_id,
                   pc.name AS category_name,
                   pc.slug AS category_slug,
                   s.id AS shop_id,
                   s.shop_name,
                   s.avatar_url,
                   s.cover_url,
                   active_price.price,
                   active_price.sale_price,
                   COALESCE(active_price.sale_price, active_price.price) AS effective_price,
                   COALESCE(pi.stock_quantity, 0) AS stock_quantity,
                   COALESCE(pi.low_stock_threshold, 0) AS low_stock_threshold,
                   COALESCE(ss.is_vacation, FALSE) AS shop_vacation,
                   ss.vacation_message
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
            WHERE p.id = :productId
              AND p.status IN ('ACTIVE', 'OUT_OF_STOCK')
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewProductDetailRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewProductDetailResult> findVisibleByProductId(UUID productId, Instant now) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("now", Timestamp.from(now));

        List<ProductHeaderRow> headers = jdbcTemplate.query(HEADER_SQL, params, this::mapHeader);
        if (headers.isEmpty()) {
            return Optional.empty();
        }

        ProductHeaderRow header = headers.getFirst();
        List<ViewProductDetailMediaItem> media = loadMedia(productId);
        List<ViewProductDetailAttributeItem> attributes = loadAttributes(productId);
        ProductReviewSummary reviewSummary = loadReviewSummary(productId);

        int stockQuantity = header.stockQuantity();
        int lowStockThreshold = header.lowStockThreshold();
        boolean inStock = stockQuantity > 0;
        boolean lowStock = inStock && stockQuantity <= lowStockThreshold;

        return Optional.of(new ViewProductDetailResult(
                header.productId(),
                header.title(),
                header.description(),
                header.condition(),
                header.weightGram(),
                header.status(),
                new ViewProductDetailCategory(
                        header.categoryId(),
                        header.categoryName(),
                        header.categorySlug()
                ),
                new ViewProductDetailShop(
                        header.shopId(),
                        header.shopName(),
                        header.avatarUrl(),
                        header.coverUrl()
                ),
                media,
                attributes,
                header.price(),
                header.salePrice(),
                header.effectivePrice(),
                new ViewProductDetailInventorySummary(
                        stockQuantity,
                        lowStockThreshold,
                        inStock,
                        lowStock
                ),
                reviewSummary.ratingAvg(),
                reviewSummary.ratingCount(),
                header.shopVacation(),
                header.vacationMessage()
        ));
    }

    private List<ViewProductDetailMediaItem> loadMedia(UUID productId) {
        String sql = """
                SELECT id, media_url, media_type, sort_order
                FROM product_media
                WHERE product_id = :productId
                ORDER BY sort_order ASC, created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new ViewProductDetailMediaItem(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("media_url"),
                        rs.getString("media_type"),
                        rs.getInt("sort_order")
                )
        );
    }

    private List<ViewProductDetailAttributeItem> loadAttributes(UUID productId) {
        String sql = """
                SELECT attribute_name, attribute_value
                FROM product_attributes
                WHERE product_id = :productId
                ORDER BY attribute_name ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new ViewProductDetailAttributeItem(
                        rs.getString("attribute_name"),
                        rs.getString("attribute_value")
                )
        );
    }

    private ProductReviewSummary loadReviewSummary(UUID productId) {
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
                return new ProductReviewSummary(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0);
            }
            BigDecimal ratingAvg = rs.getBigDecimal("rating_avg");
            return new ProductReviewSummary(
                    ratingAvg == null
                            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            : ratingAvg.setScale(2, RoundingMode.HALF_UP),
                    rs.getInt("rating_count")
            );
        });
    }

    private ProductHeaderRow mapHeader(ResultSet rs, int rowNum) throws SQLException {
        return new ProductHeaderRow(
                UUID.fromString(rs.getString("product_id")),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("condition"),
                rs.getInt("weight_gram"),
                ProductStatus.valueOf(rs.getString("product_status")),
                UUID.fromString(rs.getString("category_id")),
                rs.getString("category_name"),
                rs.getString("category_slug"),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("shop_name"),
                rs.getString("avatar_url"),
                rs.getString("cover_url"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("sale_price"),
                rs.getBigDecimal("effective_price"),
                rs.getInt("stock_quantity"),
                rs.getInt("low_stock_threshold"),
                rs.getBoolean("shop_vacation"),
                rs.getString("vacation_message")
        );
    }

    private record ProductHeaderRow(
            UUID productId,
            String title,
            String description,
            String condition,
            int weightGram,
            ProductStatus status,
            UUID categoryId,
            String categoryName,
            String categorySlug,
            UUID shopId,
            String shopName,
            String avatarUrl,
            String coverUrl,
            BigDecimal price,
            BigDecimal salePrice,
            BigDecimal effectivePrice,
            int stockQuantity,
            int lowStockThreshold,
            boolean shopVacation,
            String vacationMessage
    ) {
    }

    private record ProductReviewSummary(BigDecimal ratingAvg, int ratingCount) {
    }
}
