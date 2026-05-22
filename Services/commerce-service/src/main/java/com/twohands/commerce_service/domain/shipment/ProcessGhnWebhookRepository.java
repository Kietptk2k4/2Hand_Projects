package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProcessGhnWebhookRepository {

    Optional<SellerShipmentRecord> findByGhnOrderCodeForUpdate(String ghnOrderCode);

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

    void updateOrderItemsForShipment(UUID shipmentId, String orderItemStatus, Instant occurredAt);
}
