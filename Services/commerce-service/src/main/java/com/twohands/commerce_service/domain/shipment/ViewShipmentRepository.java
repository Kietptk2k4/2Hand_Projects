package com.twohands.commerce_service.domain.shipment;

import java.util.Optional;
import java.util.UUID;

public interface ViewShipmentRepository {

    Optional<ViewShipmentResult> findByShipmentIdAndUserId(UUID shipmentId, UUID userId);
}
