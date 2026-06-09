package com.twohands.commerce_service.domain.shipment;

import java.math.BigDecimal;
import java.util.UUID;

public record ShipmentOrderItemLine(
        UUID orderItemId,
        UUID productId,
        UUID sellerId,
        String status,
        UUID shipmentId,
        int quantity,
        BigDecimal finalPrice,
        BigDecimal shippingFeeAllocated,
        int weightGram,
        String productNameSnapshot,
        String skuSnapshot,
        BigDecimal unitPriceSnapshot
) {
    public boolean hasShipment() {
        return shipmentId != null;
    }

    public boolean isFulfillableStatus() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }
}
