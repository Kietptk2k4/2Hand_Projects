package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.TrackShipmentRepository;
import com.twohands.commerce_service.domain.shipment.TrackShipmentResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TrackShipmentRepositoryAdapter implements TrackShipmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TrackShipmentRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TrackShipmentResult> findByShipmentIdAndUserId(UUID shipmentId, UUID userId) {
        String sql = """
                SELECT s.id, s.order_id, s.seller_id,
                       s.carrier::text AS carrier,
                       s.shipment_type::text AS shipment_type,
                       s.status::text AS shipment_status,
                       s.ghn_order_code, s.tracking_number,
                       s.shipped_at, s.delivered_at, s.estimated_delivery_date,
                       o.status::text AS order_status,
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
        ShipmentStatus status = row.status();
        OrderStatus orderStatus = row.orderStatus();

        return Optional.of(new TrackShipmentResult(
                row.shipmentId(),
                row.orderId(),
                row.sellerId(),
                accessedAs,
                status,
                row.carrier(),
                row.shipmentType(),
                row.trackingNumber(),
                row.ghnOrderCode(),
                row.shippedAt(),
                row.deliveredAt(),
                row.estimatedDeliveryDate(),
                orderStatus,
                status == ShipmentStatus.DELIVERED,
                orderStatus == OrderStatus.COMPLETED,
                loadTimeline(shipmentId)
        ));
    }

    private List<ShipmentStatusHistoryEntry> loadTimeline(UUID shipmentId) {
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
        return new ShipmentAccessRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id")),
                ShipmentCarrier.valueOf(rs.getString("carrier")),
                ShipmentType.valueOf(rs.getString("shipment_type")),
                ShipmentStatus.valueOf(rs.getString("shipment_status")),
                rs.getString("ghn_order_code"),
                rs.getString("tracking_number"),
                shippedAt != null ? shippedAt.toInstant() : null,
                deliveredAt != null ? deliveredAt.toInstant() : null,
                rs.getDate("estimated_delivery_date") != null
                        ? rs.getDate("estimated_delivery_date").toLocalDate()
                        : null,
                OrderStatus.valueOf(rs.getString("order_status"))
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
            Instant shippedAt,
            Instant deliveredAt,
            LocalDate estimatedDeliveryDate,
            OrderStatus orderStatus
    ) {
    }
}
