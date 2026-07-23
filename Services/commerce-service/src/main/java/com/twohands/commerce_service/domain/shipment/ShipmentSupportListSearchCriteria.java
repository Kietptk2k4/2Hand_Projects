package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record ShipmentSupportListSearchCriteria(
        Optional<ShipmentStatus> status,
        Optional<ShipmentCarrier> carrier,
        ShipmentSupportListSortField sortField,
        Optional<String> searchQuery,
        Optional<UUID> orderId,
        Instant from,
        Instant to
) {
}
