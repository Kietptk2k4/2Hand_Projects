package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateShipmentResult(
        UUID shipmentId,
        UUID orderId,
        UUID sellerId,
        ShipmentCarrier carrier,
        ShipmentType shipmentType,
        ShipmentStatus status,
        String ghnOrderCode,
        String trackingNumber,
        BigDecimal shippingFee,
        BigDecimal codAmount,
        int weightGram,
        LocalDate estimatedDeliveryDate,
        List<UUID> orderItemIds,
        Instant createdAt
) {
}
