package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemRepository;
import com.twohands.commerce_service.domain.order.SellerOrderItemLine;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProcessSellerOrderItemRepositoryAdapter implements ProcessSellerOrderItemRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProcessSellerOrderItemRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SellerOrderItemLine> findOrderItemsBySellerAndIds(UUID sellerId, List<UUID> orderItemIds) {
        String sql = """
                SELECT id AS order_item_id,
                       order_id,
                       seller_id,
                       status::text AS item_status,
                       product_name_snapshot,
                       quantity
                FROM order_items
                WHERE seller_id = :sellerId
                  AND id IN (:orderItemIds)
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("orderItemIds", orderItemIds),
                this::mapOrderItem
        );
    }

    @Override
    public Optional<CreateShipmentOrderContext> findOrderContext(UUID orderId) {
        String sql = """
                SELECT id,
                       buyer_id,
                       status::text AS order_status,
                       payment_method::text AS payment_method,
                       payment_status::text AS payment_status,
                       final_amount
                FROM orders
                WHERE id = :orderId
                """;
        List<CreateShipmentOrderContext> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new CreateShipmentOrderContext(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("buyer_id")),
                        rs.getString("order_status"),
                        PaymentMethod.valueOf(rs.getString("payment_method")),
                        PaymentStatus.valueOf(rs.getString("payment_status")),
                        rs.getBigDecimal("final_amount")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public int markPendingItemsProcessing(UUID sellerId, List<UUID> orderItemIds, Instant occurredAt) {
        String sql = """
                UPDATE order_items
                SET status = CAST('PROCESSING' AS order_item_status),
                    updated_at = :now
                WHERE seller_id = :sellerId
                  AND id IN (:orderItemIds)
                  AND status = CAST('PENDING' AS order_item_status)
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("orderItemIds", orderItemIds)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private SellerOrderItemLine mapOrderItem(ResultSet rs, int rowNum) throws SQLException {
        return new SellerOrderItemLine(
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("item_status"),
                rs.getString("product_name_snapshot"),
                rs.getInt("quantity")
        );
    }
}
