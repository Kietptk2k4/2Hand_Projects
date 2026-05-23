package com.twohands.commerce_service.domain.shipment;

import java.util.Optional;
import java.util.UUID;

public interface ViewShipmentSupportDetailRepository {

    Optional<ShipmentSupportDetailSnapshot> findByShipmentId(UUID shipmentId);
}
