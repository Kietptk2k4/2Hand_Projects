package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
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
public class ViewSellerOrdersRepositoryAdapter implements ViewSellerOrdersRepository {

    private static final String BASE_FROM = """
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            LEFT JOIN payments p ON p.order_id = o.id
            LEFT JOIN shipments sh ON sh.id = oi.shipment_id
            LEFT JOIN shipping_address_snapshots sas ON sas.shipment_id = sh.id
            WHERE oi.seller_id = :sellerId
            """;

    private static final String SELECT_COLUMNS = """
            SELECT oi.id AS order_item_id,
                   oi.order_id,
                   oi.product_id,
                   oi.quantity,
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

    public ViewSellerOrdersRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countBySellerId(
            UUID sellerId,
            Optional<OrderItemStatus> itemStatus,
            Optional<ShipmentStatus> shipmentStatus
    ) {
        MapSqlParameterSource params = baseParams(sellerId, itemStatus, shipmentStatus);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(itemStatus, shipmentStatus),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<SellerOrderListEntry> findBySellerId(
            UUID sellerId,
            Optional<OrderItemStatus> itemStatus,
            Optional<ShipmentStatus> shipmentStatus,
            PageQuery pageQuery
    ) {
        MapSqlParameterSource params = baseParams(sellerId, itemStatus, shipmentStatus)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(itemStatus, shipmentStatus) + """
                        ORDER BY oi.created_at DESC
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );
    }

    private MapSqlParameterSource baseParams(
            UUID sellerId,
            Optional<OrderItemStatus> itemStatus,
            Optional<ShipmentStatus> shipmentStatus
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource("sellerId", sellerId);
        itemStatus.ifPresent(value -> params.addValue("itemStatus", value.name()));
        shipmentStatus.ifPresent(value -> params.addValue("shipmentStatus", value.name()));
        return params;
    }

    private String filterClause(Optional<OrderItemStatus> itemStatus, Optional<ShipmentStatus> shipmentStatus) {
        StringBuilder clause = new StringBuilder();
        if (itemStatus.isPresent()) {
            clause.append(" AND oi.status = CAST(:itemStatus AS order_item_status) ");
        }
        if (shipmentStatus.isPresent()) {
            clause.append(" AND sh.status = CAST(:shipmentStatus AS shipment_status) ");
        }
        return clause.toString();
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
