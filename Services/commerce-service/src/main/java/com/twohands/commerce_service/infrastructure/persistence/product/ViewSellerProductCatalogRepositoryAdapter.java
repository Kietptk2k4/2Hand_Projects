package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.SellerProductAttributeItem;
import com.twohands.commerce_service.domain.product.SellerProductDetail;
import com.twohands.commerce_service.domain.product.SellerProductListItem;
import com.twohands.commerce_service.domain.product.SellerProductListSummary;
import com.twohands.commerce_service.domain.product.ViewSellerProductCatalogRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewSellerProductCatalogRepositoryAdapter implements ViewSellerProductCatalogRepository {

    private static final String LIST_SELECT = """
            SELECT p.id AS product_id,
                   p.seller_id,
                   p.shop_id,
                   p.status::text AS product_status,
                   p.product_type,
                   p.category_id,
                   pc.name AS category_name,
                   p.condition,
                   p.title,
                   p.description,
                   p.weight_gram,
                   thumbnail.media_url AS thumbnail_url,
                   active_price.price,
                   active_price.sale_price,
                   active_price.effective_price,
                   pi.stock_quantity,
                   pi.low_stock_threshold,
                   p.created_at,
                   p.updated_at
            """;

    private static final String COUNT_FROM = """
            FROM products p
            WHERE p.seller_id = :sellerId
            """;

    private static final String LIST_FROM = """
            FROM products p
            INNER JOIN product_categories pc ON pc.id = p.category_id
            LEFT JOIN product_inventories pi ON pi.product_id = p.id
            LEFT JOIN LATERAL (
                SELECT pp.price,
                       pp.sale_price,
                       COALESCE(pp.sale_price, pp.price) AS effective_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :now
                  AND (pp.end_at IS NULL OR pp.end_at > :now)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN LATERAL (
                SELECT pm.media_url
                FROM product_media pm
                WHERE pm.product_id = p.id
                  AND pm.media_type = 'IMAGE'
                ORDER BY pm.sort_order ASC, pm.created_at ASC
                LIMIT 1
            ) thumbnail ON TRUE
            WHERE p.seller_id = :sellerId
            """;

    private static final String DETAIL_HEADER_SQL = """
            SELECT p.id AS product_id,
                   p.seller_id,
                   p.shop_id,
                   p.status::text AS product_status,
                   p.product_type,
                   p.category_id,
                   pc.name AS category_name,
                   p.brand_id,
                   p.condition,
                   p.title,
                   p.description,
                   p.weight_gram,
                   thumbnail.media_url AS thumbnail_url,
                   active_price.id AS price_id,
                   active_price.price,
                   active_price.sale_price,
                   active_price.effective_price,
                   pi.stock_quantity,
                   pi.low_stock_threshold,
                   pi.reserved_quantity,
                   pi.product_id IS NOT NULL AS has_inventory_row,
                   p.created_at,
                   p.updated_at
            FROM products p
            INNER JOIN product_categories pc ON pc.id = p.category_id
            LEFT JOIN product_inventories pi ON pi.product_id = p.id
            LEFT JOIN LATERAL (
                SELECT pp.id, pp.price, pp.sale_price, COALESCE(pp.sale_price, pp.price) AS effective_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :now
                  AND (pp.end_at IS NULL OR pp.end_at > :now)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN LATERAL (
                SELECT pm.media_url
                FROM product_media pm
                WHERE pm.product_id = p.id
                  AND pm.media_type = 'IMAGE'
                ORDER BY pm.sort_order ASC, pm.created_at ASC
                LIMIT 1
            ) thumbnail ON TRUE
            WHERE p.id = :productId
              AND p.seller_id = :sellerId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewSellerProductCatalogRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countBySellerId(UUID sellerId, Optional<ProductStatus> status, Optional<String> keyword) {
        MapSqlParameterSource params = filterParams(sellerId, status, keyword, null);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + COUNT_FROM + filterClause(status, keyword),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<SellerProductListItem> findBySellerId(
            UUID sellerId,
            Optional<ProductStatus> status,
            Optional<String> keyword,
            PageQuery pageQuery,
            Instant now
    ) {
        MapSqlParameterSource params = filterParams(sellerId, status, keyword, now)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(
                LIST_SELECT + LIST_FROM + filterClause(status, keyword) + """
                        ORDER BY p.updated_at DESC
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapListItem
        );
    }

    @Override
    public SellerProductListSummary summarizeBySellerId(UUID sellerId) {
        String sql = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN p.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count,
                       SUM(CASE WHEN p.status = 'OUT_OF_STOCK' THEN 1 ELSE 0 END) AS out_of_stock_count,
                       SUM(CASE WHEN p.status = 'DRAFT' THEN 1 ELSE 0 END) AS draft_count,
                       SUM(CASE WHEN p.status = 'PAUSED' THEN 1 ELSE 0 END) AS paused_count,
                       SUM(CASE WHEN p.status = 'ARCHIVED' THEN 1 ELSE 0 END) AS archived_count,
                       SUM(CASE
                               WHEN p.status = 'ACTIVE'
                                    AND COALESCE(pi.stock_quantity, 0) > 0
                                    AND COALESCE(pi.stock_quantity, 0) <= COALESCE(pi.low_stock_threshold, 0)
                               THEN 1
                               ELSE 0
                           END) AS low_stock_count
                FROM products p
                LEFT JOIN product_inventories pi ON pi.product_id = p.id
                WHERE p.seller_id = :sellerId
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                rs -> {
                    if (!rs.next()) {
                        return new SellerProductListSummary(0, 0, 0, 0, 0, 0, 0);
                    }
                    return new SellerProductListSummary(
                            rs.getLong("total"),
                            rs.getLong("active_count"),
                            rs.getLong("out_of_stock_count"),
                            rs.getLong("draft_count"),
                            rs.getLong("paused_count"),
                            rs.getLong("archived_count"),
                            rs.getLong("low_stock_count")
                    );
                }
        );
    }

    @Override
    public Optional<SellerProductDetail> findDetailBySellerId(UUID sellerId, UUID productId, Instant now) {
        List<DetailHeaderRow> headers = jdbcTemplate.query(
                DETAIL_HEADER_SQL,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId)
                        .addValue("now", Timestamp.from(now)),
                this::mapDetailHeader
        );
        if (headers.isEmpty()) {
            return Optional.empty();
        }

        DetailHeaderRow header = headers.getFirst();
        List<String> mediaUrls = loadMediaUrls(productId);
        List<SellerProductAttributeItem> attributes = loadAttributes(productId);

        String thumbnailUrl = header.thumbnailUrl();
        boolean hasMedia = isHttpUrl(thumbnailUrl) || !mediaUrls.isEmpty();
        boolean hasPrice = header.priceId() != null && header.effectivePrice() != null;

        return Optional.of(new SellerProductDetail(
                header.productId(),
                header.sellerId(),
                header.shopId(),
                header.status(),
                header.productType(),
                header.categoryId(),
                header.categoryName(),
                header.brandId(),
                header.condition(),
                header.title(),
                header.description(),
                header.weightGram(),
                thumbnailUrl,
                header.price(),
                header.salePrice(),
                header.effectivePrice(),
                header.priceId(),
                header.stockQuantity(),
                header.lowStockThreshold(),
                header.reservedQuantity(),
                attributes,
                mediaUrls,
                hasPrice,
                header.hasInventoryRow(),
                hasMedia,
                header.createdAt(),
                header.updatedAt()
        ));
    }

    private List<String> loadMediaUrls(UUID productId) {
        String sql = """
                SELECT media_url
                FROM product_media
                WHERE product_id = :productId
                ORDER BY sort_order ASC, created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> rs.getString("media_url")
        );
    }

    private List<SellerProductAttributeItem> loadAttributes(UUID productId) {
        String sql = """
                SELECT attribute_name, attribute_value
                FROM product_attributes
                WHERE product_id = :productId
                ORDER BY attribute_name ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new SellerProductAttributeItem(
                        rs.getString("attribute_name"),
                        rs.getString("attribute_value")
                )
        );
    }

    private MapSqlParameterSource filterParams(
            UUID sellerId,
            Optional<ProductStatus> status,
            Optional<String> keyword,
            Instant now
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource("sellerId", sellerId);
        if (now != null) {
            params.addValue("now", Timestamp.from(now));
        }
        status.ifPresent(value -> params.addValue("status", value.name()));
        keyword.filter(StringUtils::hasText).ifPresent(value -> params.addValue("pattern", toLikePattern(value)));
        return params;
    }

    private String filterClause(Optional<ProductStatus> status, Optional<String> keyword) {
        StringBuilder clause = new StringBuilder();
        status.ifPresent(ignored -> clause.append(" AND p.status::text = :status"));
        keyword.filter(StringUtils::hasText).ifPresent(ignored -> clause.append(" AND p.title ILIKE :pattern ESCAPE '\\'"));
        return clause.toString();
    }

    private String toLikePattern(String keyword) {
        String escaped = keyword.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private SellerProductListItem mapListItem(ResultSet rs, int rowNum) throws SQLException {
        return new SellerProductListItem(
                UUID.fromString(rs.getString("product_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status")),
                rs.getString("product_type"),
                UUID.fromString(rs.getString("category_id")),
                rs.getString("category_name"),
                rs.getString("condition"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("weight_gram"),
                rs.getString("thumbnail_url"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("sale_price"),
                rs.getBigDecimal("effective_price"),
                getNullableInt(rs, "stock_quantity"),
                getNullableInt(rs, "low_stock_threshold"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private DetailHeaderRow mapDetailHeader(ResultSet rs, int rowNum) throws SQLException {
        String brandId = rs.getString("brand_id");
        String priceId = rs.getString("price_id");
        return new DetailHeaderRow(
                UUID.fromString(rs.getString("product_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status")),
                rs.getString("product_type"),
                UUID.fromString(rs.getString("category_id")),
                rs.getString("category_name"),
                brandId == null ? null : UUID.fromString(brandId),
                rs.getString("condition"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("weight_gram"),
                rs.getString("thumbnail_url"),
                priceId == null ? null : UUID.fromString(priceId),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("sale_price"),
                rs.getBigDecimal("effective_price"),
                getNullableInt(rs, "stock_quantity"),
                getNullableInt(rs, "low_stock_threshold"),
                getNullableInt(rs, "reserved_quantity"),
                rs.getBoolean("has_inventory_row"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private record DetailHeaderRow(
            UUID productId,
            UUID sellerId,
            UUID shopId,
            ProductStatus status,
            String productType,
            UUID categoryId,
            String categoryName,
            UUID brandId,
            String condition,
            String title,
            String description,
            int weightGram,
            String thumbnailUrl,
            UUID priceId,
            BigDecimal price,
            BigDecimal salePrice,
            BigDecimal effectivePrice,
            Integer stockQuantity,
            Integer lowStockThreshold,
            Integer reservedQuantity,
            boolean hasInventoryRow,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
