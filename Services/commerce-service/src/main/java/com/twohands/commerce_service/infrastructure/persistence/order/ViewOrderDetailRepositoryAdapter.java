package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.ShippingAddressSnapshot;
import com.twohands.commerce_service.domain.order.ViewOrderDetailItem;
import com.twohands.commerce_service.domain.order.ViewOrderDetailPaymentSummary;
import com.twohands.commerce_service.domain.order.PaymentRefundRequestRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.domain.order.ViewOrderDetailShipment;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewOrderDetailRepositoryAdapter implements ViewOrderDetailRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PaymentRefundRequestRepository paymentRefundRequestRepository;

    public ViewOrderDetailRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            PaymentRefundRequestRepository paymentRefundRequestRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.paymentRefundRequestRepository = paymentRefundRequestRepository;
    }

    @Override
    public Optional<ViewOrderDetailResult> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId) {
        return buildResult(loadOrderForBuyer(orderId, buyerId));
    }

    @Override
    public Optional<ViewOrderDetailResult> findByOrderId(UUID orderId) {
        return buildResult(loadOrderById(orderId));
    }

    private Optional<ViewOrderDetailResult> buildResult(OrderHeaderRow order) {
        if (order == null) {
            return Optional.empty();
        }

        UUID orderId = order.orderId();
        ViewOrderDetailPaymentSummary payment = loadPayment(orderId);
        List<ViewOrderDetailItem> items = loadOrderItems(orderId);
        List<ViewOrderDetailShipment> shipments = loadShipments(orderId);
        List<OrderStatusHistoryEntry> orderTimeline = loadOrderTimeline(orderId);

        return Optional.of(new ViewOrderDetailResult(
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
                paymentRefundRequestRepository.findSummaryByOrderId(orderId).orElse(null),
                loadCancellationNote(orderId)
        ));
    }

    private String loadCancellationNote(UUID orderId) {
        String sql = """
                SELECT note
                FROM order_status_history
                WHERE order_id = :orderId
                  AND new_status = 'CANCELLED'
                ORDER BY created_at DESC
                LIMIT 1
                """;
        List<String> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> rs.getString("note")
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private OrderHeaderRow loadOrderForBuyer(UUID orderId, UUID buyerId) {
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

    private OrderHeaderRow loadOrderById(UUID orderId) {
        String sql = """
                SELECT id, buyer_id, status::text AS order_status, payment_status::text AS order_payment_status,
                       payment_method::text AS payment_method, total_amount, final_amount,
                       created_at, updated_at, completed_at
                FROM orders
                WHERE id = :orderId
                """;
        List<OrderHeaderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapOrderHeader(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private ViewOrderDetailPaymentSummary loadPayment(UUID orderId) {
        String sql = """
                SELECT id, status::text AS payment_status, payment_method::text AS payment_method,
                       amount, currency, paid_at, expired_at, checkout_url_expired_at
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
        return new ViewOrderDetailPaymentSummary(
                payment.paymentId(),
                payment.status(),
                payment.paymentMethod(),
                payment.amount(),
                payment.currency(),
                payment.paidAt(),
                payment.expiredAt(),
                payment.checkoutUrlExpiredAt(),
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

    private List<ViewOrderDetailItem> loadOrderItems(UUID orderId) {
        String sql = """
                SELECT oi.id,
                       oi.product_id,
                       oi.seller_id,
                       oi.shipment_id,
                       oi.quantity,
                       oi.status::text AS item_status,
                       oi.unit_price_snapshot,
                       oi.final_price,
                       oi.sku_snapshot,
                       oi.product_name_snapshot,
                       oi.image_snapshot,
                       oi.attributes_snapshot::text AS attributes_snapshot,
                       oi.shop_name_snapshot,
                       oi.shipping_fee_allocated,
                       oi.completed_at,
                       r.id AS review_id
                FROM order_items oi
                LEFT JOIN reviews r ON r.order_item_id = oi.id
                WHERE oi.order_id = :orderId
                ORDER BY oi.created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new ViewOrderDetailItem(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("product_id")),
                        UUID.fromString(rs.getString("seller_id")),
                        optionalUuid(rs.getString("shipment_id")),
                        rs.getInt("quantity"),
                        OrderItemStatus.valueOf(rs.getString("item_status")),
                        rs.getBigDecimal("unit_price_snapshot"),
                        rs.getBigDecimal("final_price"),
                        rs.getString("sku_snapshot"),
                        rs.getString("product_name_snapshot"),
                        rs.getString("image_snapshot"),
                        rs.getString("attributes_snapshot"),
                        rs.getString("shop_name_snapshot"),
                        rs.getBigDecimal("shipping_fee_allocated"),
                        optionalInstant(rs.getTimestamp("completed_at")),
                        optionalUuid(rs.getString("review_id"))
                )
        );
    }

    private List<ViewOrderDetailShipment> loadShipments(UUID orderId) {
        String sql = """
                SELECT s.id, s.seller_id, s.status::text AS shipment_status, s.carrier::text AS carrier,
                       s.tracking_number, s.shipping_fee, s.shipment_type::text AS shipment_type,
                       s.estimated_delivery_date, s.shipped_at, s.delivered_at,
                       sas.receiver_name, sas.phone, sas.province_code, sas.district_code,
                       sas.ward_code, sas.address_detail, sas.full_address
                FROM shipments s
                LEFT JOIN shipping_address_snapshots sas ON sas.shipment_id = s.id
                WHERE s.order_id = :orderId
                ORDER BY s.created_at ASC
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

        List<ViewOrderDetailShipment> shipments = new ArrayList<>();
        for (ShipmentRow row : rows) {
            shipments.add(new ViewOrderDetailShipment(
                    row.shipmentId(),
                    row.sellerId(),
                    row.status(),
                    row.carrier(),
                    row.trackingNumber(),
                    row.shippingFee(),
                    row.shipmentType(),
                    row.estimatedDeliveryDate(),
                    row.shippedAt(),
                    row.deliveredAt(),
                    row.shippingAddress(),
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
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                optionalInstant(rs.getTimestamp("paid_at")),
                optionalInstant(rs.getTimestamp("expired_at")),
                optionalInstant(rs.getTimestamp("checkout_url_expired_at"))
        );
    }

    private ShipmentRow mapShipmentRow(ResultSet rs) throws SQLException {
        ShippingAddressSnapshot address = null;
        String receiverName = rs.getString("receiver_name");
        if (receiverName != null) {
            address = new ShippingAddressSnapshot(
                    receiverName,
                    rs.getString("phone"),
                    rs.getString("province_code"),
                    rs.getString("district_code"),
                    rs.getString("ward_code"),
                    rs.getString("address_detail"),
                    rs.getString("full_address")
            );
        }
        return new ShipmentRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                ShipmentStatus.valueOf(rs.getString("shipment_status")),
                rs.getString("carrier"),
                rs.getString("tracking_number"),
                rs.getBigDecimal("shipping_fee"),
                ShipmentType.valueOf(rs.getString("shipment_type")),
                optionalLocalDate(rs.getDate("estimated_delivery_date")),
                optionalInstant(rs.getTimestamp("shipped_at")),
                optionalInstant(rs.getTimestamp("delivered_at")),
                address
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

    private static LocalDate optionalLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
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
            BigDecimal amount,
            String currency,
            Instant paidAt,
            Instant expiredAt,
            Instant checkoutUrlExpiredAt
    ) {
    }

    private record ShipmentRow(
            UUID shipmentId,
            UUID sellerId,
            ShipmentStatus status,
            String carrier,
            String trackingNumber,
            BigDecimal shippingFee,
            ShipmentType shipmentType,
            LocalDate estimatedDeliveryDate,
            Instant shippedAt,
            Instant deliveredAt,
            ShippingAddressSnapshot shippingAddress
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
