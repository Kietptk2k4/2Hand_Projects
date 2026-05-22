package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductInventoryState;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryRepository;
import com.twohands.commerce_service.domain.product.UpdateProductInventorySnapshot;
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
public class UpdateProductInventoryRepositoryAdapter implements UpdateProductInventoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateProductInventoryRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateProductInventorySnapshot> findProductForInventoryUpdate(
            UUID productId,
            UUID sellerId,
            Instant now
    ) {
        String sql = """
                SELECT p.id,
                       p.seller_id,
                       p.shop_id,
                       p.status::text AS product_status,
                       s.status AS shop_status,
                       pc.is_active AS category_active,
                       pi.stock_quantity,
                       pi.low_stock_threshold,
                       pi.reserved_quantity,
                       pp.price AS active_price
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
        List<UpdateProductInventorySnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId)
                        .addValue("now", Timestamp.from(now)),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public ProductInventoryState upsertInventory(
            UUID productId,
            int stockQuantity,
            int lowStockThreshold,
            Instant now
    ) {
        int existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_inventories WHERE product_id = :productId",
                new MapSqlParameterSource("productId", productId),
                Integer.class
        );

        if (existing == 0) {
            jdbcTemplate.update(
                    """
                            INSERT INTO product_inventories (
                                product_id, stock_quantity, low_stock_threshold,
                                reserved_quantity, created_at, updated_at
                            )
                            VALUES (
                                :productId, :stockQuantity, :lowStockThreshold,
                                0, :now, :now
                            )
                            """,
                    new MapSqlParameterSource()
                            .addValue("productId", productId)
                            .addValue("stockQuantity", stockQuantity)
                            .addValue("lowStockThreshold", lowStockThreshold)
                            .addValue("now", Timestamp.from(now))
            );
        } else {
            jdbcTemplate.update(
                    """
                            UPDATE product_inventories
                            SET stock_quantity = :stockQuantity,
                                low_stock_threshold = :lowStockThreshold,
                                updated_at = :now
                            WHERE product_id = :productId
                            """,
                    new MapSqlParameterSource()
                            .addValue("productId", productId)
                            .addValue("stockQuantity", stockQuantity)
                            .addValue("lowStockThreshold", lowStockThreshold)
                            .addValue("now", Timestamp.from(now))
            );
        }

        return loadInventory(productId);
    }

    @Override
    public void updateProductStatus(UUID productId, ProductStatus status, Instant now) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE products
                        SET status = CAST(:status AS product_status),
                            updated_at = :now
                        WHERE id = :productId
                        """,
                new MapSqlParameterSource()
                        .addValue("status", status.name())
                        .addValue("now", Timestamp.from(now))
                        .addValue("productId", productId)
        );
        if (updated == 0) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private ProductInventoryState loadInventory(UUID productId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT stock_quantity, low_stock_threshold, reserved_quantity
                        FROM product_inventories
                        WHERE product_id = :productId
                        """,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new ProductInventoryState(
                        rs.getInt("stock_quantity"),
                        rs.getInt("low_stock_threshold"),
                        rs.getInt("reserved_quantity")
                )
        );
    }

    private UpdateProductInventorySnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        ProductInventoryState inventory = null;
        if (rs.getObject("stock_quantity") != null) {
            inventory = new ProductInventoryState(
                    rs.getInt("stock_quantity"),
                    rs.getInt("low_stock_threshold"),
                    rs.getInt("reserved_quantity")
            );
        }

        return new UpdateProductInventorySnapshot(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status")),
                rs.getString("shop_status"),
                rs.getBoolean("category_active"),
                rs.getBigDecimal("active_price"),
                inventory
        );
    }
}
