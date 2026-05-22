package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewOrderDetailShipment(
        UUID shipmentId,
        UUID sellerId,
        ShipmentStatus status,
        String carrier,
        String trackingNumber,
        BigDecimal shippingFee,
        ShipmentType shipmentType,
        LocalDate estimatedDeliveryDate,
        Instant shippedAt,
        Instant deliveredAt,
        ShippingAddressSnapshot shippingAddress,
        List<ShipmentStatusHistoryEntry> timeline
) {
}
