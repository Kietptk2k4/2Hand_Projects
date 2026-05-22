package com.twohands.commerce_service.domain.shipment;

import java.util.UUID;

public record ShipmentOrderItemSummary(
        UUID orderItemId,
        String productNameSnapshot,
        int quantity,
        String status
) {
}
