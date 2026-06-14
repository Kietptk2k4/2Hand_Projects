package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
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
public class ViewSellerOrderDetailRepositoryAdapter implements ViewSellerOrderDetailRepository {

    private static final String ITEMS_FROM = """
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            LEFT JOIN products prod ON prod.id = oi.product_id
            LEFT JOIN payments p ON p.order_id = o.id
            LEFT JOIN shipments sh ON sh.id = oi.shipment_id
            LEFT JOIN shipping_address_snapshots sas ON sas.shipment_id = sh.id
            WHERE oi.seller_id = :sellerId
              AND oi.order_id = :orderId
            """;

    private static final String ITEMS_SELECT = """
            SELECT oi.id AS order_item_id,
                   oi.order_id,
                   oi.product_id,
                   oi.quantity,
                   COALESCE(prod.weight_gram, 0) * oi.quantity AS line_weight_gram,
                   oi.unit_price_snapshot,
                   oi.final_price,
                   oi.shipping_fee_allocated,
                   oi.product_name_snapshot,
                   oi.image_snapshot,
                   oi.status::text AS item_status,
                   oi.created_at AS item_created_at,
                   oi.updated_at AS item_updated_at,
                   o.status::text AS order_status,
                   o.payment_status::text AS order_payment_status,
                   o.payment_method::text AS order_payment_method,
                   o.created_at AS order_created_at,
                   p.id AS payment_id,
                   p.status::text AS payment_status,
                   p.payment_method::text AS payment_method,
                   p.amount AS payment_amount,
                   p.currency AS payment_currency,
                   sh.id AS shipment_id,
                   sh.status::text AS shipment_status,
                   sh.carrier::text AS shipment_carrier,
                   sh.tracking_number,
                   sas.full_address AS delivery_address_summary
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewSellerOrderDetailRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewSellerOrderDetailResult> findSellerOrderDetail(UUID sellerId, UUID orderId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("orderId", orderId);

        List<SellerOrderListEntry> items = jdbcTemplate.query(
                ITEMS_SELECT + ITEMS_FROM + " ORDER BY oi.created_at ASC",
                params,
                this::mapEntry
        );
        if (items.isEmpty()) {
            return Optional.empty();
        }

        SellerOrderListEntry first = items.getFirst();
        BigDecimal itemsSubtotal = items.stream()
                .map(SellerOrderListEntry::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingTotal = items.stream()
                .map(SellerOrderListEntry::shippingFeeAllocated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ShipmentAddressSnapshot shippingAddress = resolveShippingAddress(first.orderStatus(), items);
        UUID buyerId = loadOrderBuyerId(sellerId, orderId);

        return Optional.of(new ViewSellerOrderDetailResult(
                first.orderId(),
                first.orderStatus(),
                first.orderPaymentStatus(),
                first.orderPaymentMethod(),
                first.orderCreatedAt(),
                first.payment(),
                itemsSubtotal,
                shippingTotal,
                items,
                shippingAddress,
                new CommerceBuyerSummary(buyerId, null, null)
        ));
    }

    private ShipmentAddressSnapshot resolveShippingAddress(
            OrderStatus orderStatus,
            List<SellerOrderListEntry> items
    ) {
        if (orderStatus != OrderStatus.PROCESSING && orderStatus != OrderStatus.COMPLETED) {
            return null;
        }

        UUID shipmentId = items.stream()
                .map(item -> item.shipment().shipmentId())
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);
        if (shipmentId == null) {
            return null;
        }

        return loadAddressSnapshot(shipmentId).orElse(null);
    }

    private Optional<ShipmentAddressSnapshot> loadAddressSnapshot(UUID shipmentId) {
        String sql = """
                SELECT receiver_name, phone, province_code, district_code,
                       ward_code, address_detail, full_address
                FROM shipping_address_snapshots
                WHERE shipment_id = :shipmentId
                """;
        List<ShipmentAddressSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shipmentId", shipmentId),
                (rs, rowNum) -> new ShipmentAddressSnapshot(
                        rs.getString("receiver_name"),
                        rs.getString("phone"),
                        rs.getString("province_code"),
                        rs.getString("district_code"),
                        rs.getString("ward_code"),
                        rs.getString("address_detail"),
                        rs.getString("full_address")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private UUID loadOrderBuyerId(UUID sellerId, UUID orderId) {
        String sql = """
                SELECT o.buyer_id
                FROM order_items oi
                INNER JOIN orders o ON o.id = oi.order_id
                WHERE oi.seller_id = :sellerId
                  AND oi.order_id = :orderId
                LIMIT 1
                """;
        List<UUID> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("buyer_id"))
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private SellerOrderListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        String paymentId = rs.getString("payment_id");
        SellerOrderListPaymentSummary payment = paymentId == null
                ? null
                : new SellerOrderListPaymentSummary(
                        UUID.fromString(paymentId),
                        PaymentStatus.valueOf(rs.getString("payment_status")),
                        PaymentMethod.valueOf(rs.getString("payment_method")),
                        rs.getBigDecimal("payment_amount"),
                        rs.getString("payment_currency")
                );

        String shipmentId = rs.getString("shipment_id");
        SellerOrderListShipmentSummary shipment = shipmentId == null
                ? SellerOrderListShipmentSummary.empty()
                : new SellerOrderListShipmentSummary(
                        UUID.fromString(shipmentId),
                        ShipmentStatus.valueOf(rs.getString("shipment_status")),
                        ShipmentCarrier.valueOf(rs.getString("shipment_carrier")),
                        rs.getString("tracking_number"),
                        rs.getString("delivery_address_summary")
                );

        return new SellerOrderListEntry(
                UUID.fromString(rs.getString("order_item_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("product_id")),
                rs.getInt("quantity"),
                rs.getInt("line_weight_gram"),
                rs.getBigDecimal("unit_price_snapshot"),
                rs.getBigDecimal("final_price"),
                rs.getBigDecimal("shipping_fee_allocated"),
                rs.getString("product_name_snapshot"),
                rs.getString("image_snapshot"),
                OrderItemStatus.valueOf(rs.getString("item_status")),
                toInstant(rs.getTimestamp("item_created_at")),
                toInstant(rs.getTimestamp("item_updated_at")),
                OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("order_payment_status")),
                PaymentMethod.valueOf(rs.getString("order_payment_method")),
                toInstant(rs.getTimestamp("order_created_at")),
                payment,
                shipment
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
