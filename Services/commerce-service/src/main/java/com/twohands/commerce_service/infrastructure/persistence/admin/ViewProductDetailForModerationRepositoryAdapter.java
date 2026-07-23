package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.domain.admin.AdminProductDetailAttributeItem;
import com.twohands.commerce_service.domain.admin.AdminProductDetailEntry;
import com.twohands.commerce_service.domain.admin.AdminProductDetailMediaItem;
import com.twohands.commerce_service.domain.admin.ViewProductDetailForModerationRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewProductDetailForModerationRepositoryAdapter implements ViewProductDetailForModerationRepository {

    private static final String DETAIL_QUERY = """
            SELECT p.id AS product_id,
                   p.seller_id,
                   p.shop_id,
                   ss.shop_name,
                   p.title,
                   p.description,
                   p.status::text AS status,
                   p.category_id,
                   pc.name AS category_name,
                   active_price.price,
                   active_price.effective_price,
                   COALESCE(pi.stock_quantity, 0) AS stock_quantity,
                   p.created_at,
                   p.updated_at,
                   CASE WHEN p.status::text = 'REMOVED' THEN p.updated_at ELSE NULL END AS removed_at,
                   p.remove_reason,
                   COALESCE(oc.open_count, 0) AS open_order_count
            FROM products p
            INNER JOIN seller_shops ss ON ss.id = p.shop_id
            INNER JOIN product_categories pc ON pc.id = p.category_id
            LEFT JOIN LATERAL (
                SELECT pp.price,
                       COALESCE(pp.sale_price, pp.price) AS effective_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :now
                  AND (pp.end_at IS NULL OR pp.end_at > :now)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN product_inventories pi ON pi.product_id = p.id
            LEFT JOIN (
                SELECT oi.product_id, COUNT(DISTINCT o.id) AS open_count
                FROM orders o
                INNER JOIN order_items oi ON oi.order_id = o.id
                WHERE o.status NOT IN ('COMPLETED', 'CANCELLED')
                GROUP BY oi.product_id
            ) oc ON oc.product_id = p.id
            WHERE p.id = :productId
            """;

    private static final String MEDIA_QUERY = """
            SELECT media_url, media_type, sort_order
            FROM product_media
            WHERE product_id = :productId
            ORDER BY sort_order ASC, created_at ASC
            """;

    private static final String ATTRIBUTES_QUERY = """
            SELECT attribute_name, attribute_value
            FROM product_attributes
            WHERE product_id = :productId
            ORDER BY attribute_name ASC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Clock clock;

    public ViewProductDetailForModerationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public Optional<AdminProductDetailEntry> findByProductId(UUID productId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("now", Timestamp.from(clock.instant()));

        List<AdminProductDetailEntry> rows = jdbcTemplate.query(DETAIL_QUERY, params, this::mapEntry);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private AdminProductDetailEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        UUID productId = rs.getObject("product_id", UUID.class);
        List<AdminProductDetailMediaItem> media = loadMedia(productId);
        List<AdminProductDetailAttributeItem> attributes = loadAttributes(productId);

        return new AdminProductDetailEntry(
                productId,
                rs.getObject("seller_id", UUID.class),
                rs.getObject("shop_id", UUID.class),
                rs.getString("shop_name"),
                rs.getString("title"),
                rs.getString("description"),
                ProductStatus.valueOf(rs.getString("status")),
                rs.getObject("category_id", UUID.class),
                rs.getString("category_name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("effective_price"),
                rs.getInt("stock_quantity"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at")),
                toInstant(rs.getTimestamp("removed_at")),
                rs.getString("remove_reason"),
                rs.getLong("open_order_count"),
                media,
                attributes
        );
    }

    private List<AdminProductDetailMediaItem> loadMedia(UUID productId) {
        return jdbcTemplate.query(
                MEDIA_QUERY,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new AdminProductDetailMediaItem(
                        rs.getString("media_url"),
                        rs.getString("media_type"),
                        rs.getInt("sort_order")
                )
        );
    }

    private List<AdminProductDetailAttributeItem> loadAttributes(UUID productId) {
        return jdbcTemplate.query(
                ATTRIBUTES_QUERY,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new AdminProductDetailAttributeItem(
                        rs.getString("attribute_name"),
                        rs.getString("attribute_value")
                )
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
