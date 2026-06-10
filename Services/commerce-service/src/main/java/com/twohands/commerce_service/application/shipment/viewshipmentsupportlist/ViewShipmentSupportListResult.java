package com.twohands.commerce_service.application.shipment.viewshipmentsupportlist;

import com.twohands.commerce_service.domain.shipment.ShipmentSupportListEntry;

import java.util.List;

public record ViewShipmentSupportListResult(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<ShipmentSupportListEntry> shipments
) {
}
