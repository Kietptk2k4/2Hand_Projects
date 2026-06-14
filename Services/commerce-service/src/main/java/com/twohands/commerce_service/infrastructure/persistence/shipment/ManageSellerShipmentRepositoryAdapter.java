package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ManageSellerShipmentRepositoryAdapter implements ManageSellerShipmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ManageSellerShipmentRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SellerShipmentRecord> findShipmentForSeller(UUID shipmentId, UUID sellerId) {
        return queryShipment(shipmentId, sellerId);
    }

    @Override
    public Optional<SellerShipmentDetail> findDetailForSeller(UUID shipmentId, UUID sellerId) {
        Optional<SellerShipmentRecord> shipment = queryShipment(shipmentId, sellerId);
        if (shipment.isEmpty()) {
            return Optional.empty();
        }
        ShipmentAddressSnapshot address = loadAddressSnapshot(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Shipping address snapshot missing"));
        List<ShipmentOrderItemSummary> items = loadOrderItems(shipmentId);
        UUID buyerId = loadOrderBuyerId(shipment.get().orderId());
        return Optional.of(new SellerShipmentDetail(
                shipment.get(),
                address,
                items,
                new CommerceBuyerSummary(buyerId, null, null)
        ));
    }

    @Override
    public boolean updateTrackingOnly(UUID shipmentId, UUID sellerId, String trackingNumber, Instant occurredAt) {
        try {
            String sql = """
                    UPDATE shipments
                    SET tracking_number = :trackingNumber,
                        updated_at = :now
                    WHERE id = :shipmentId
                      AND seller_id = :sellerId
                      AND status::text NOT IN ('DELIVERED', 'CANCELLED', 'FAILED', 'RETURNED')
                    """;
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("shipmentId", shipmentId)
                    .addValue("sellerId", sellerId)
                    .addValue("trackingNumber", trackingNumber)
                    .addValue("now", Timestamp.from(occurredAt)));
            return updated == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.DUPLICATE_TRACKING_NUMBER, "Tracking number already exists", ex);
        }
    }

    @Override
    public boolean updateStatusAndTracking(
            UUID shipmentId,
            UUID sellerId,
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus,
            String trackingNumber,
            Instant occurredAt
    ) {
        try {
            String sql = """
                    UPDATE shipments
                    SET status = CAST(:newStatus AS shipment_status),
                        tracking_number = COALESCE(:trackingNumber, tracking_number),
                        shipped_at = CASE
                            WHEN CAST(:newStatus AS shipment_status) = 'SHIPPED'::shipment_status
                                AND shipped_at IS NULL THEN :now
                            ELSE shipped_at
                        END,
                        delivered_at = CASE
                            WHEN CAST(:newStatus AS shipment_status) = 'DELIVERED'::shipment_status
                                THEN :now
                            ELSE delivered_at
                        END,
                        updated_at = :now
                    WHERE id = :shipmentId
                      AND seller_id = :sellerId
                      AND status = CAST(:currentStatus AS shipment_status)
                    """;
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("shipmentId", shipmentId)
                    .addValue("sellerId", sellerId)
                    .addValue("currentStatus", currentStatus.name())
                    .addValue("newStatus", newStatus.name())
                    .addValue("trackingNumber", trackingNumber)
                    .addValue("now", Timestamp.from(occurredAt)));
            return updated == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.DUPLICATE_TRACKING_NUMBER, "Tracking number already exists", ex);
        }
    }

    @Override
    public void insertStatusHistory(
            UUID shipmentId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            Instant occurredAt
    ) {
        String sql = """
                INSERT INTO shipment_status_history(
                    id, shipment_id, old_status, new_status, created_at
                ) VALUES (
                    :id, :shipmentId,
                    CAST(:oldStatus AS shipment_status),
                    CAST(:newStatus AS shipment_status),
                    :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("shipmentId", shipmentId)
                .addValue("oldStatus", oldStatus.name())
                .addValue("newStatus", newStatus.name())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    @Override
    public void updateOrderItemsForShipment(UUID shipmentId, String orderItemStatus, Instant occurredAt) {
        String sql = """
                UPDATE order_items
                SET status = CAST(:itemStatus AS order_item_status),
                    updated_at = :now
                WHERE shipment_id = :shipmentId
                  AND status::text NOT IN ('COMPLETED', 'CANCELLED')
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("itemStatus", orderItemStatus)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private Optional<SellerShipmentRecord> queryShipment(UUID shipmentId, UUID sellerId) {
        String sql = """
                SELECT id, order_id, seller_id,
                       carrier::text AS carrier,
                       shipment_type::text AS shipment_type,
                       status::text AS status,
                       ghn_order_code, tracking_number,
                       shipping_fee, cod_amount, weight_gram,
                       estimated_delivery_date,
                       shipped_at, delivered_at, created_at, updated_at
                FROM shipments
                WHERE id = :shipmentId AND seller_id = :sellerId
                """;
        List<SellerShipmentRecord> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("shipmentId", shipmentId)
                        .addValue("sellerId", sellerId),
                this::mapShipment
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
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

    private UUID loadOrderBuyerId(UUID orderId) {
        String sql = """
                SELECT buyer_id
                FROM orders
                WHERE id = :orderId
                """;
        List<UUID> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("buyer_id"))
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private List<ShipmentOrderItemSummary> loadOrderItems(UUID shipmentId) {
        String sql = """
                SELECT id, product_name_snapshot, quantity, status::text AS item_status
                FROM order_items
                WHERE shipment_id = :shipmentId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shipmentId", shipmentId),
                (rs, rowNum) -> new ShipmentOrderItemSummary(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("product_name_snapshot"),
                        rs.getInt("quantity"),
                        rs.getString("item_status")
                )
        );
    }

    private SellerShipmentRecord mapShipment(ResultSet rs, int rowNum) throws SQLException {
        Timestamp shippedAt = rs.getTimestamp("shipped_at");
        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        int weight = rs.getInt("weight_gram");
        return new SellerShipmentRecord(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("seller_id")),
                ShipmentCarrier.valueOf(rs.getString("carrier")),
                ShipmentType.valueOf(rs.getString("shipment_type")),
                ShipmentStatus.valueOf(rs.getString("status")),
                rs.getString("ghn_order_code"),
                rs.getString("tracking_number"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("cod_amount"),
                rs.wasNull() ? null : weight,
                rs.getDate("estimated_delivery_date") != null
                        ? rs.getDate("estimated_delivery_date").toLocalDate()
                        : null,
                shippedAt != null ? shippedAt.toInstant() : null,
                deliveredAt != null ? deliveredAt.toInstant() : null,
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
