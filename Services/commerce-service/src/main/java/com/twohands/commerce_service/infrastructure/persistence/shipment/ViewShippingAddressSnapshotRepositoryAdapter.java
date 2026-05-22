package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotRepository;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewShippingAddressSnapshotRepositoryAdapter implements ViewShippingAddressSnapshotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewShippingAddressSnapshotRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewShippingAddressSnapshotResult> findByShipmentIdAndUserId(UUID shipmentId, UUID userId) {
        String ownershipSql = """
                SELECT s.seller_id, o.buyer_id
                FROM shipments s
                INNER JOIN orders o ON o.id = s.order_id
                WHERE s.id = :shipmentId
                  AND (s.seller_id = :userId OR o.buyer_id = :userId)
                """;
        List<OwnershipRow> ownershipRows = jdbcTemplate.query(
                ownershipSql,
                new MapSqlParameterSource()
                        .addValue("shipmentId", shipmentId)
                        .addValue("userId", userId),
                (rs, rowNum) -> new OwnershipRow(
                        UUID.fromString(rs.getString("seller_id")),
                        UUID.fromString(rs.getString("buyer_id"))
                )
        );
        if (ownershipRows.isEmpty()) {
            return Optional.empty();
        }

        OwnershipRow ownership = ownershipRows.getFirst();
        ShipmentAccessRole accessedAs = ownership.sellerId().equals(userId)
                ? ShipmentAccessRole.SELLER
                : ShipmentAccessRole.BUYER;

        String snapshotSql = """
                SELECT id, receiver_name, phone, province_code, district_code,
                       ward_code, address_detail, full_address, created_at
                FROM shipping_address_snapshots
                WHERE shipment_id = :shipmentId
                """;
        List<ViewShippingAddressSnapshotResult> snapshots = jdbcTemplate.query(
                snapshotSql,
                new MapSqlParameterSource("shipmentId", shipmentId),
                (rs, rowNum) -> new ViewShippingAddressSnapshotResult(
                        shipmentId,
                        UUID.fromString(rs.getString("id")),
                        new ShipmentAddressSnapshot(
                                rs.getString("receiver_name"),
                                rs.getString("phone"),
                                rs.getString("province_code"),
                                rs.getString("district_code"),
                                rs.getString("ward_code"),
                                rs.getString("address_detail"),
                                rs.getString("full_address")
                        ),
                        accessedAs,
                        rs.getTimestamp("created_at").toInstant()
                )
        );
        return snapshots.isEmpty() ? Optional.empty() : Optional.of(snapshots.getFirst());
    }

    private record OwnershipRow(UUID sellerId, UUID buyerId) {
    }
}
