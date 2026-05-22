package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductPublishSnapshot;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.PublishProductRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
public class PublishProductRepositoryAdapter implements PublishProductRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PublishProductRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProductPublishSnapshot> findForSeller(UUID productId, UUID sellerId, Instant now) {
        String sql = """
                SELECT p.id,
                       p.seller_id,
                       p.shop_id,
                       p.title,
                       p.description,
                       p.condition,
                       p.weight_gram,
                       p.status AS product_status,
                       p.updated_at,
                       s.status AS shop_status,
                       pc.is_active AS category_active,
                       pi.stock_quantity,
                       pp.price AS active_price,
                       (
                           SELECT pm.media_url
                           FROM product_media pm
                           WHERE pm.product_id = p.id
                           ORDER BY pm.sort_order ASC
                           LIMIT 1
                       ) AS primary_media_url
                FROM products p
                INNER JOIN seller_shops s ON s.id = p.shop_id
                INNER JOIN product_categories pc ON pc.id = p.category_id
                LEFT JOIN product_inventories pi ON pi.product_id = p.id
                LEFT JOIN LATERAL (
                    SELECT price
                    FROM product_prices
                    WHERE product_id = p.id
                      AND start_at <= :now
                      AND (end_at IS NULL OR end_at > :now)
                    ORDER BY start_at DESC
                    LIMIT 1
                ) pp ON TRUE
                WHERE p.id = :productId
                  AND p.seller_id = :sellerId
                """;
        List<ProductPublishSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId)
                        .addValue("now", Timestamp.from(now)),
                (rs, rowNum) -> mapSnapshot(rs)
        );
        return rows.stream().findFirst();
    }

    @Override
    public ProductPublishSnapshot updateStatus(UUID productId, ProductStatus status, Instant updatedAt) {
        String sql = """
                UPDATE products
                SET status = CAST(:status AS product_status),
                    updated_at = :updatedAt
                WHERE id = :productId
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("updatedAt", Timestamp.from(updatedAt))
                .addValue("productId", productId));
        if (updated == 0) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return findForSeller(productId, loadSellerId(productId), updatedAt)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private UUID loadSellerId(UUID productId) {
        return jdbcTemplate.queryForObject(
                "SELECT seller_id FROM products WHERE id = :productId",
                new MapSqlParameterSource("productId", productId),
                UUID.class
        );
    }

    private ProductPublishSnapshot mapSnapshot(ResultSet rs) throws SQLException {
        return new ProductPublishSnapshot(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("condition"),
                rs.getInt("weight_gram"),
                ProductStatus.valueOf(rs.getString("product_status")),
                rs.getString("shop_status"),
                rs.getBoolean("category_active"),
                rs.getBigDecimal("active_price"),
                rs.getObject("stock_quantity") == null ? null : rs.getInt("stock_quantity"),
                rs.getString("primary_media_url"),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
