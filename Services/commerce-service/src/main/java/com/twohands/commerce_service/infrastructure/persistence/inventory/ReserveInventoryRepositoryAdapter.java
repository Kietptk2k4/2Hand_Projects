package com.twohands.commerce_service.infrastructure.persistence.inventory;

import com.twohands.commerce_service.domain.inventory.InventoryReservationLine;
import com.twohands.commerce_service.domain.inventory.ReserveInventoryRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.UUID;

@Repository
public class ReserveInventoryRepositoryAdapter implements ReserveInventoryRepository {

    private static final Logger log = LoggerFactory.getLogger(ReserveInventoryRepositoryAdapter.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReserveInventoryRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void reserveAll(List<InventoryReservationLine> lines, Instant updatedAt) {
        List<UUID> productIds = lines.stream().map(InventoryReservationLine::productId).toList();
        lockInventories(productIds);
        ensureInventoryRecordsExist(productIds);

        String sql = """
                UPDATE product_inventories
                SET stock_quantity = stock_quantity - :quantity,
                    reserved_quantity = reserved_quantity + :quantity,
                    updated_at = :now
                WHERE product_id = :productId
                  AND stock_quantity >= :quantity
                """;
        for (InventoryReservationLine line : lines) {
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("productId", line.productId())
                    .addValue("quantity", line.quantity())
                    .addValue("now", Timestamp.from(updatedAt)));
            if (updated == 0) {
                log.warn("Stock conflict during inventory reservation for product {}", line.productId());
                throw new AppException(ErrorCode.OUT_OF_STOCK, "Insufficient stock for checkout");
            }
        }
    }

    @Override
    public void syncOutOfStockProductStatuses(List<UUID> productIds, Instant updatedAt) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        String sql = """
                UPDATE products p
                SET status = CAST('OUT_OF_STOCK' AS product_status),
                    updated_at = :now
                FROM product_inventories pi
                WHERE p.id = pi.product_id
                  AND p.id IN (:productIds)
                  AND p.status = CAST('ACTIVE' AS product_status)
                  AND pi.stock_quantity = 0
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("productIds", productIds)
                .addValue("now", Timestamp.from(updatedAt)));
    }

    @Override
    public void syncInStockProductStatuses(List<UUID> productIds, Instant updatedAt) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        String sql = """
                UPDATE products p
                SET status = CAST('ACTIVE' AS product_status),
                    updated_at = :now
                FROM product_inventories pi
                WHERE p.id = pi.product_id
                  AND p.id IN (:productIds)
                  AND p.status = CAST('OUT_OF_STOCK' AS product_status)
                  AND pi.stock_quantity > 0
                  AND EXISTS (
                      SELECT 1
                      FROM seller_shops s
                      WHERE s.id = p.shop_id
                        AND s.status = CAST('ACTIVE' AS shop_status)
                  )
                  AND EXISTS (
                      SELECT 1
                      FROM product_categories pc
                      WHERE pc.id = p.category_id
                        AND pc.is_active = TRUE
                  )
                  AND EXISTS (
                      SELECT 1
                      FROM product_prices pp
                      WHERE pp.product_id = p.id
                        AND pp.start_at <= :now
                        AND (pp.end_at IS NULL OR pp.end_at > :now)
                  )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("productIds", productIds)
                .addValue("now", Timestamp.from(updatedAt)));
    }

    private void lockInventories(List<UUID> productIds) {
        if (productIds.isEmpty()) {
            return;
        }
        String sql = """
                SELECT product_id
                FROM product_inventories
                WHERE product_id IN (:productIds)
                ORDER BY product_id
                FOR UPDATE
                """;
        jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productIds", productIds),
                (rs, rowNum) -> rs.getString("product_id")
        );
    }

    private void ensureInventoryRecordsExist(List<UUID> productIds) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM product_inventories
                        WHERE product_id IN (:productIds)
                        """,
                new MapSqlParameterSource("productIds", productIds),
                Integer.class
        );
        if (count == null || count != productIds.size()) {
            throw new AppException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory record is missing for a product");
        }
    }
}
