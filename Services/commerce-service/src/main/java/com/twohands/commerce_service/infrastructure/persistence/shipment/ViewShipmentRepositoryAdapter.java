package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ViewShipmentRepository;
import com.twohands.commerce_service.domain.shipment.ViewShipmentResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewShipmentRepositoryAdapter implements ViewShipmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewShipmentRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewShipmentResult> findByShipmentIdAndUserId(UUID shipmentId, UUID userId) {
        String sql = """
                SELECT s.id, s.order_id, s.seller_id,
                       s.carrier::text AS carrier,
                       s.shipment_type::text AS shipment_type,
                       s.status::text AS status,
                       s.ghn_order_code, s.tracking_number,
                       s.shipping_fee, s.cod_amount, s.weight_gram,
                       s.estimated_delivery_date,
                       s.shipped_at, s.delivered_at, s.created_at, s.updated_at,
                       o.buyer_id
                FROM shipments s
                INNER JOIN orders o ON o.id = s.order_id
                WHERE s.id = :shipmentId
                  AND (s.seller_id = :userId OR o.buyer_id = :userId)
                """;
        List<ShipmentAccessRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("shipmentId", shipmentId)
                        .addValue("userId", userId),
                this::mapAccessRow
        );
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        ShipmentAccessRow row = rows.getFirst();
        ShipmentAccessRole accessedAs = row.sellerId().equals(userId)
                ? ShipmentAccessRole.SELLER
                : ShipmentAccessRole.BUYER;

        ShipmentAddressSnapshot address = loadAddressSnapshot(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Shipping address snapshot missing"));

        return Optional.of(new ViewShipmentResult(
                row.toShipmentRecord(),
                address,
                loadOrderItems(shipmentId),
                loadStatusHistory(shipmentId),
                accessedAs
        ));
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

    private List<ShipmentStatusHistoryEntry> loadStatusHistory(UUID shipmentId) {
        String sql = """
                SELECT old_status::text AS old_status, new_status::text AS new_status,
                       raw_status, created_at
                FROM shipment_status_history
                WHERE shipment_id = :shipmentId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shipmentId", shipmentId),
                (rs, rowNum) -> new ShipmentStatusHistoryEntry(
                        parseShipmentStatus(rs.getString("old_status")),
                        ShipmentStatus.valueOf(rs.getString("new_status")),
                        rs.getString("raw_status"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private ShipmentAccessRow mapAccessRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp shippedAt = rs.getTimestamp("shipped_at");
        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        int weight = rs.getInt("weight_gram");
        return new ShipmentAccessRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id")),
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

    private static ShipmentStatus parseShipmentStatus(String value) {
        return value == null ? null : ShipmentStatus.valueOf(value);
    }

    private record ShipmentAccessRow(
            UUID shipmentId,
            UUID orderId,
            UUID sellerId,
            UUID buyerId,
            ShipmentCarrier carrier,
            ShipmentType shipmentType,
            ShipmentStatus status,
            String ghnOrderCode,
            String trackingNumber,
            BigDecimal shippingFee,
            BigDecimal codAmount,
            Integer weightGram,
            LocalDate estimatedDeliveryDate,
            Instant shippedAt,
            Instant deliveredAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        SellerShipmentRecord toShipmentRecord() {
            return new SellerShipmentRecord(
                    shipmentId,
                    orderId,
                    sellerId,
                    carrier,
                    shipmentType,
                    status,
                    ghnOrderCode,
                    trackingNumber,
                    shippingFee,
                    codAmount,
                    weightGram,
                    estimatedDeliveryDate,
                    shippedAt,
                    deliveredAt,
                    createdAt,
                    updatedAt
            );
        }
    }
}
