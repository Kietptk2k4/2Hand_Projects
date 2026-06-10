package com.twohands.commerce_service.domain.shipment;

import java.util.Optional;

public record ShipmentSupportListSearchCriteria(
        Optional<ShipmentStatus> status,
        Optional<ShipmentCarrier> carrier,
        ShipmentSupportListSortField sortField
) {
}
