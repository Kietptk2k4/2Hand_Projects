package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.OrderListEntry;
import com.twohands.commerce_service.domain.order.OrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.OrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.ViewOrderListRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewOrderListRepositoryAdapter implements ViewOrderListRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewOrderListRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countByBuyerId(UUID buyerId, Optional<OrderStatus> status) {
        MapSqlParameterSource params = baseParams(buyerId, status);
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM orders
                        WHERE buyer_id = :buyerId
                        """ + statusClause(status),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<OrderListEntry> findByBuyerId(UUID buyerId, Optional<OrderStatus> status, PageQuery pageQuery) {
        List<OrderHeaderRow> orders = loadOrders(buyerId, status, pageQuery);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<UUID> orderIds = orders.stream().map(OrderHeaderRow::orderId).toList();
        Map<UUID, ItemAggregateRow> itemAggregates = loadItemAggregates(orderIds);
        Map<UUID, OrderListPaymentSummary> payments = loadPayments(orderIds);
        Map<UUID, OrderListShipmentSummary> shipments = loadShipmentSummaries(orderIds);

        List<OrderListEntry> entries = new ArrayList<>();
        for (OrderHeaderRow order : orders) {
            ItemAggregateRow itemAggregate = itemAggregates.getOrDefault(
                    order.orderId(),
                    new ItemAggregateRow(0, null, null)
            );
            entries.add(new OrderListEntry(
                    order.orderId(),
                    order.orderStatus(),
                    order.orderPaymentStatus(),
                    order.paymentMethod(),
                    order.totalAmount(),
                    order.finalAmount(),
                    order.createdAt(),
                    order.updatedAt(),
                    order.completedAt(),
                    itemAggregate.itemCount(),
                    itemAggregate.previewProductName(),
                    itemAggregate.previewImageUrl(),
                    payments.get(order.orderId()),
                    shipments.getOrDefault(order.orderId(), OrderListShipmentSummary.empty())
            ));
        }
        return entries;
    }

    private List<OrderHeaderRow> loadOrders(UUID buyerId, Optional<OrderStatus> status, PageQuery pageQuery) {
        MapSqlParameterSource params = baseParams(buyerId, status)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());
        return jdbcTemplate.query(
                """
                        SELECT id, status::text AS order_status, payment_status::text AS order_payment_status,
                               payment_method::text AS payment_method, total_amount, final_amount,
                               created_at, updated_at, completed_at
                        FROM orders
                        WHERE buyer_id = :buyerId
                        """ + statusClause(status) + """
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                (rs, rowNum) -> new OrderHeaderRow(
                        UUID.fromString(rs.getString("id")),
                        OrderStatus.valueOf(rs.getString("order_status")),
                        PaymentStatus.valueOf(rs.getString("order_payment_status")),
                        PaymentMethod.valueOf(rs.getString("payment_method")),
                        rs.getBigDecimal("total_amount"),
                        rs.getBigDecimal("final_amount"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant(),
                        optionalInstant(rs.getTimestamp("completed_at"))
                )
        );
    }

    private Map<UUID, ItemAggregateRow> loadItemAggregates(List<UUID> orderIds) {
        Map<UUID, ItemAggregateRow> aggregates = new HashMap<>();
        jdbcTemplate.query(
                """
                        SELECT order_id, COUNT(*)::int AS item_count
                        FROM order_items
                        WHERE order_id IN (:orderIds)
                        GROUP BY order_id
                        """,
                new MapSqlParameterSource("orderIds", orderIds),
                (rs, rowNum) -> {
                    aggregates.put(
                            UUID.fromString(rs.getString("order_id")),
                            new ItemAggregateRow(rs.getInt("item_count"), null, null)
                    );
                    return null;
                }
        );

        jdbcTemplate.query(
                """
                        SELECT DISTINCT ON (order_id)
                               order_id, product_name_snapshot, image_snapshot
                        FROM order_items
                        WHERE order_id IN (:orderIds)
                        ORDER BY order_id, created_at ASC
                        """,
                new MapSqlParameterSource("orderIds", orderIds),
                (rs, rowNum) -> {
                    UUID orderId = UUID.fromString(rs.getString("order_id"));
                    ItemAggregateRow existing = aggregates.getOrDefault(orderId, new ItemAggregateRow(0, null, null));
                    aggregates.put(
                            orderId,
                            new ItemAggregateRow(
                                    existing.itemCount(),
                                    rs.getString("product_name_snapshot"),
                                    rs.getString("image_snapshot")
                            )
                    );
                    return null;
                }
        );
        return aggregates;
    }

    private Map<UUID, OrderListPaymentSummary> loadPayments(List<UUID> orderIds) {
        Map<UUID, OrderListPaymentSummary> payments = new HashMap<>();
        jdbcTemplate.query(
                """
                        SELECT order_id, id, status::text AS payment_status, payment_method::text AS payment_method,
                               amount, currency
                        FROM payments
                        WHERE order_id IN (:orderIds)
                        """,
                new MapSqlParameterSource("orderIds", orderIds),
                (rs, rowNum) -> {
                    payments.put(
                            UUID.fromString(rs.getString("order_id")),
                            new OrderListPaymentSummary(
                                    UUID.fromString(rs.getString("id")),
                                    PaymentStatus.valueOf(rs.getString("payment_status")),
                                    PaymentMethod.valueOf(rs.getString("payment_method")),
                                    rs.getBigDecimal("amount"),
                                    rs.getString("currency")
                            )
                    );
                    return null;
                }
        );
        return payments;
    }

    private Map<UUID, OrderListShipmentSummary> loadShipmentSummaries(List<UUID> orderIds) {
        Map<UUID, OrderListShipmentSummary> shipments = new HashMap<>();
        jdbcTemplate.query(
                """
                        SELECT order_id,
                               COUNT(*)::int AS shipment_count,
                               COALESCE(array_agg(DISTINCT status::text), ARRAY[]::text[]) AS statuses
                        FROM shipments
                        WHERE order_id IN (:orderIds)
                        GROUP BY order_id
                        """,
                new MapSqlParameterSource("orderIds", orderIds),
                (rs, rowNum) -> {
                    shipments.put(
                            UUID.fromString(rs.getString("order_id")),
                            new OrderListShipmentSummary(
                                    rs.getInt("shipment_count"),
                                    parseShipmentStatuses(rs.getArray("statuses"))
                            )
                    );
                    return null;
                }
        );
        return shipments;
    }

    private List<ShipmentStatus> parseShipmentStatuses(Array sqlArray) {
        if (sqlArray == null) {
            return List.of();
        }
        try {
            Object array = sqlArray.getArray();
            if (array == null) {
                return List.of();
            }
            return Arrays.stream((Object[]) array)
                    .map(Object::toString)
                    .map(ShipmentStatus::valueOf)
                    .toList();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read shipment statuses", ex);
        }
    }

    private MapSqlParameterSource baseParams(UUID buyerId, Optional<OrderStatus> status) {
        MapSqlParameterSource params = new MapSqlParameterSource("buyerId", buyerId);
        status.ifPresent(value -> params.addValue("status", value.name()));
        return params;
    }

    private String statusClause(Optional<OrderStatus> status) {
        return status.isPresent() ? " AND status = CAST(:status AS order_status) " : "";
    }

    private static Instant optionalInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private record OrderHeaderRow(
            UUID orderId,
            OrderStatus orderStatus,
            PaymentStatus orderPaymentStatus,
            PaymentMethod paymentMethod,
            BigDecimal totalAmount,
            BigDecimal finalAmount,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt
    ) {
    }

    private record ItemAggregateRow(
            int itemCount,
            String previewProductName,
            String previewImageUrl
    ) {
    }
}
