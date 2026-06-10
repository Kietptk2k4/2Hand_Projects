package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProcessGhnWebhookRepository {

    Optional<SellerShipmentRecord> findByGhnOrderCodeForUpdate(String ghnOrderCode);

    Optional<SellerShipmentRecord> findByShipmentIdForUpdate(UUID shipmentId);

    Optional<SellerShipmentRecord> findGhnShipmentForUserUpdate(UUID shipmentId, UUID userId);

    void updateTrackingNumberIfBlank(UUID shipmentId, String trackingNumber, Instant occurredAt);

    boolean updateStatus(
            UUID shipmentId,
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus,
            Instant occurredAt
    );

    void insertStatusHistory(
            UUID shipmentId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String rawStatus,
            Instant occurredAt
    );

    int updateOrderItemsForShipment(UUID shipmentId, String orderItemStatus, Instant occurredAt);
}
