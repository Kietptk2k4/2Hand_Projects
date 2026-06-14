package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
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
public class ProcessGhnWebhookRepositoryAdapter implements ProcessGhnWebhookRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProcessGhnWebhookRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SellerShipmentRecord> findByGhnOrderCodeForUpdate(String ghnOrderCode) {
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
                WHERE ghn_order_code = :ghnOrderCode
                FOR UPDATE
                """;
        List<SellerShipmentRecord> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("ghnOrderCode", ghnOrderCode),
                this::mapShipment
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<SellerShipmentRecord> findByShipmentIdForUpdate(UUID shipmentId) {
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
                WHERE id = :shipmentId
                FOR UPDATE
                """;
        List<SellerShipmentRecord> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shipmentId", shipmentId),
                this::mapShipment
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<SellerShipmentRecord> findGhnShipmentForUserUpdate(UUID shipmentId, UUID userId) {
        String sql = """
                SELECT s.id, s.order_id, s.seller_id,
                       s.carrier::text AS carrier,
                       s.shipment_type::text AS shipment_type,
                       s.status::text AS status,
                       s.ghn_order_code, s.tracking_number,
                       s.shipping_fee, s.cod_amount, s.weight_gram,
                       s.estimated_delivery_date,
                       s.shipped_at, s.delivered_at, s.created_at, s.updated_at
                FROM shipments s
                INNER JOIN orders o ON o.id = s.order_id
                WHERE s.id = :shipmentId
                  AND s.carrier = 'GHN'::shipment_carrier
                  AND (s.seller_id = :userId OR o.buyer_id = :userId)
                FOR UPDATE
                """;
        List<SellerShipmentRecord> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("shipmentId", shipmentId)
                        .addValue("userId", userId),
                this::mapShipment
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public void updateTrackingNumberIfBlank(UUID shipmentId, String trackingNumber, Instant occurredAt) {
        String sql = """
                UPDATE shipments
                SET tracking_number = :trackingNumber,
                    updated_at = :now
                WHERE id = :shipmentId
                  AND (tracking_number IS NULL OR BTRIM(tracking_number) = '')
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("trackingNumber", trackingNumber)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    @Override
    public boolean updateStatus(
            UUID shipmentId,
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE shipments
                SET status = CAST(:newStatus AS shipment_status),
                    shipped_at = CASE
                        WHEN CAST(:newStatus AS shipment_status) IN (
                            'SHIPPED'::shipment_status,
                            'DELIVERED'::shipment_status
                        ) AND shipped_at IS NULL THEN :now
                        ELSE shipped_at
                    END,
                    delivered_at = CASE
                        WHEN CAST(:newStatus AS shipment_status) = 'DELIVERED'::shipment_status
                            THEN :now
                        ELSE delivered_at
                    END,
                    updated_at = :now
                WHERE id = :shipmentId
                  AND status = CAST(:currentStatus AS shipment_status)
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("currentStatus", currentStatus.name())
                .addValue("newStatus", newStatus.name())
                .addValue("now", Timestamp.from(occurredAt)));
        return updated == 1;
    }

    @Override
    public void insertStatusHistory(
            UUID shipmentId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String rawStatus,
            Instant occurredAt
    ) {
        String sql = """
                INSERT INTO shipment_status_history(
                    id, shipment_id, old_status, new_status, raw_status, created_at
                ) VALUES (
                    :id, :shipmentId,
                    CAST(:oldStatus AS shipment_status),
                    CAST(:newStatus AS shipment_status),
                    :rawStatus,
                    :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("shipmentId", shipmentId)
                .addValue("oldStatus", oldStatus.name())
                .addValue("newStatus", newStatus.name())
                .addValue("rawStatus", rawStatus)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    @Override
    public int updateOrderItemsForShipment(UUID shipmentId, String orderItemStatus, Instant occurredAt) {
        String sql = """
                UPDATE order_items
                SET status = CAST(:itemStatus AS order_item_status),
                    updated_at = :now
                WHERE shipment_id = :shipmentId
                  AND status::text NOT IN ('COMPLETED', 'CANCELLED')
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("itemStatus", orderItemStatus)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    @Override
    public int releaseOrderItemsFromCancelledShipment(UUID shipmentId, Instant occurredAt) {
        String sql = """
                UPDATE order_items
                SET shipment_id = NULL,
                    status = 'PROCESSING',
                    updated_at = :now
                WHERE shipment_id = :shipmentId
                  AND status::text NOT IN ('COMPLETED', 'CANCELLED')
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("now", Timestamp.from(occurredAt)));
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
