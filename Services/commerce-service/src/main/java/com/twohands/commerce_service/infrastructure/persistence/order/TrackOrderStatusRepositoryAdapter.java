package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderItemTrackingLine;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.TrackOrderStatusRepository;
import com.twohands.commerce_service.domain.order.TrackOrderStatusResult;
import com.twohands.commerce_service.domain.payment.OrderPaymentTracking;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentTrackingLine;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TrackOrderStatusRepositoryAdapter implements TrackOrderStatusRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TrackOrderStatusRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TrackOrderStatusResult> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId) {
        OrderHeaderRow order = loadOrder(orderId, buyerId);
        if (order == null) {
            return Optional.empty();
        }

        OrderPaymentTracking payment = loadPayment(orderId);
        List<OrderItemTrackingLine> items = loadOrderItems(orderId);
        List<ShipmentTrackingLine> shipments = loadShipments(orderId);
        List<OrderStatusHistoryEntry> orderTimeline = loadOrderTimeline(orderId);

        boolean paymentPaid = payment != null && payment.status() == PaymentStatus.PAID;
        boolean allItemsCompleted = !items.isEmpty()
                && items.stream().allMatch(item -> item.status() == OrderItemStatus.COMPLETED);
        boolean anyShipmentDelivered = shipments.stream()
                .anyMatch(shipment -> shipment.status() == ShipmentStatus.DELIVERED);
        boolean anyItemDelivered = items.stream()
                .anyMatch(item -> item.status() == OrderItemStatus.DELIVERED);
        boolean orderCompleted = order.orderStatus() == OrderStatus.COMPLETED;

        return Optional.of(new TrackOrderStatusResult(
                order.orderId(),
                order.buyerId(),
                order.orderStatus(),
                order.orderPaymentStatus(),
                order.paymentMethod(),
                order.totalAmount(),
                order.finalAmount(),
                order.createdAt(),
                order.updatedAt(),
                order.completedAt(),
                payment,
                items,
                shipments,
                orderTimeline,
                orderCompleted,
                paymentPaid,
                allItemsCompleted,
                anyShipmentDelivered,
                anyItemDelivered
        ));
    }

    private OrderHeaderRow loadOrder(UUID orderId, UUID buyerId) {
        String sql = """
                SELECT id, buyer_id, status::text AS order_status, payment_status::text AS order_payment_status,
                       payment_method::text AS payment_method, total_amount, final_amount,
                       created_at, updated_at, completed_at
                FROM orders
                WHERE id = :orderId AND buyer_id = :buyerId
                """;
        List<OrderHeaderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapOrderHeader(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private OrderPaymentTracking loadPayment(UUID orderId) {
        String sql = """
                SELECT id, status::text AS payment_status, payment_method::text AS payment_method,
                       paid_at, expired_at
                FROM payments
                WHERE order_id = :orderId
                """;
        List<PaymentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapPaymentRow(rs)
        );
        if (rows.isEmpty()) {
            return null;
        }
        PaymentRow payment = rows.getFirst();
        List<PaymentStatusHistoryEntry> timeline = loadPaymentTimeline(payment.paymentId());
        return new OrderPaymentTracking(
                payment.paymentId(),
                payment.status(),
                payment.paymentMethod(),
                payment.paidAt(),
                payment.expiredAt(),
                timeline
        );
    }

    private List<PaymentStatusHistoryEntry> loadPaymentTimeline(UUID paymentId) {
        String sql = """
                SELECT old_status::text AS old_status, new_status::text AS new_status, created_at
                FROM payment_status_history
                WHERE payment_id = :paymentId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("paymentId", paymentId),
                (rs, rowNum) -> new PaymentStatusHistoryEntry(
                        parsePaymentStatus(rs.getString("old_status")),
                        PaymentStatus.valueOf(rs.getString("new_status")),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private List<OrderItemTrackingLine> loadOrderItems(UUID orderId) {
        String sql = """
                SELECT id, product_id, seller_id, product_name_snapshot, quantity,
                       status::text AS item_status, shipment_id, completed_at
                FROM order_items
                WHERE order_id = :orderId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new OrderItemTrackingLine(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("product_id")),
                        UUID.fromString(rs.getString("seller_id")),
                        rs.getString("product_name_snapshot"),
                        rs.getInt("quantity"),
                        OrderItemStatus.valueOf(rs.getString("item_status")),
                        optionalUuid(rs.getString("shipment_id")),
                        optionalInstant(rs.getTimestamp("completed_at"))
                )
        );
    }

    private List<ShipmentTrackingLine> loadShipments(UUID orderId) {
        String sql = """
                SELECT id, seller_id, status::text AS shipment_status, carrier::text AS carrier,
                       tracking_number, shipped_at, delivered_at
                FROM shipments
                WHERE order_id = :orderId
                ORDER BY created_at ASC
                """;
        List<ShipmentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapShipmentRow(rs)
        );
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<ShipmentStatusHistoryEntry>> timelines = loadShipmentTimelines(
                rows.stream().map(ShipmentRow::shipmentId).toList()
        );

        List<ShipmentTrackingLine> shipments = new ArrayList<>();
        for (ShipmentRow row : rows) {
            shipments.add(new ShipmentTrackingLine(
                    row.shipmentId(),
                    row.sellerId(),
                    row.status(),
                    row.carrier(),
                    row.trackingNumber(),
                    row.shippedAt(),
                    row.deliveredAt(),
                    timelines.getOrDefault(row.shipmentId(), List.of())
            ));
        }
        return shipments;
    }

    private Map<UUID, List<ShipmentStatusHistoryEntry>> loadShipmentTimelines(List<UUID> shipmentIds) {
        String sql = """
                SELECT shipment_id, old_status::text AS old_status, new_status::text AS new_status,
                       raw_status, created_at
                FROM shipment_status_history
                WHERE shipment_id IN (:shipmentIds)
                ORDER BY created_at ASC
                """;
        List<ShipmentHistoryRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shipmentIds", shipmentIds),
                (rs, rowNum) -> new ShipmentHistoryRow(
                        UUID.fromString(rs.getString("shipment_id")),
                        parseShipmentStatus(rs.getString("old_status")),
                        ShipmentStatus.valueOf(rs.getString("new_status")),
                        rs.getString("raw_status"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );

        Map<UUID, List<ShipmentStatusHistoryEntry>> grouped = new LinkedHashMap<>();
        for (ShipmentHistoryRow row : rows) {
            grouped.computeIfAbsent(row.shipmentId(), ignored -> new ArrayList<>())
                    .add(new ShipmentStatusHistoryEntry(
                            row.oldStatus(),
                            row.newStatus(),
                            row.rawStatus(),
                            row.occurredAt()
                    ));
        }
        return grouped;
    }

    private List<OrderStatusHistoryEntry> loadOrderTimeline(UUID orderId) {
        String sql = """
                SELECT old_status::text AS old_status, new_status::text AS new_status,
                       changed_by, note, created_at
                FROM order_status_history
                WHERE order_id = :orderId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new OrderStatusHistoryEntry(
                        parseOrderStatus(rs.getString("old_status")),
                        OrderStatus.valueOf(rs.getString("new_status")),
                        rs.getString("changed_by"),
                        rs.getString("note"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private OrderHeaderRow mapOrderHeader(ResultSet rs) throws SQLException {
        return new OrderHeaderRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("buyer_id")),
                OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("order_payment_status")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("final_amount"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                optionalInstant(rs.getTimestamp("completed_at"))
        );
    }

    private PaymentRow mapPaymentRow(ResultSet rs) throws SQLException {
        return new PaymentRow(
                UUID.fromString(rs.getString("id")),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                optionalInstant(rs.getTimestamp("paid_at")),
                optionalInstant(rs.getTimestamp("expired_at"))
        );
    }

    private ShipmentRow mapShipmentRow(ResultSet rs) throws SQLException {
        return new ShipmentRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                ShipmentStatus.valueOf(rs.getString("shipment_status")),
                rs.getString("carrier"),
                rs.getString("tracking_number"),
                optionalInstant(rs.getTimestamp("shipped_at")),
                optionalInstant(rs.getTimestamp("delivered_at"))
        );
    }

    private static OrderStatus parseOrderStatus(String value) {
        return value == null ? null : OrderStatus.valueOf(value);
    }

    private static PaymentStatus parsePaymentStatus(String value) {
        return value == null ? null : PaymentStatus.valueOf(value);
    }

    private static ShipmentStatus parseShipmentStatus(String value) {
        return value == null ? null : ShipmentStatus.valueOf(value);
    }

    private static UUID optionalUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static Instant optionalInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private record OrderHeaderRow(
            UUID orderId,
            UUID buyerId,
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

    private record PaymentRow(
            UUID paymentId,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            Instant paidAt,
            Instant expiredAt
    ) {
    }

    private record ShipmentRow(
            UUID shipmentId,
            UUID sellerId,
            ShipmentStatus status,
            String carrier,
            String trackingNumber,
            Instant shippedAt,
            Instant deliveredAt
    ) {
    }

    private record ShipmentHistoryRow(
            UUID shipmentId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String rawStatus,
            Instant occurredAt
    ) {
    }
}
