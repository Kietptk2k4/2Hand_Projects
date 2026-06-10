package com.twohands.commerce_service.domain.shipment;

import java.util.List;

public record ShipmentSupportListPagedResult(
        List<ShipmentSupportListEntry> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
