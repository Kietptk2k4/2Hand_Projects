package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ManageSellerShipmentRepository {

    Optional<SellerShipmentRecord> findShipmentForSeller(UUID shipmentId, UUID sellerId);

    Optional<SellerShipmentDetail> findDetailForSeller(UUID shipmentId, UUID sellerId);

    boolean updateTrackingOnly(UUID shipmentId, UUID sellerId, String trackingNumber, Instant occurredAt);

    boolean updateStatusAndTracking(
            UUID shipmentId,
            UUID sellerId,
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus,
            String trackingNumber,
            Instant occurredAt
    );

    void insertStatusHistory(
            UUID shipmentId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            Instant occurredAt
    );

    void updateOrderItemsForShipment(UUID shipmentId, String orderItemStatus, Instant occurredAt);
}
