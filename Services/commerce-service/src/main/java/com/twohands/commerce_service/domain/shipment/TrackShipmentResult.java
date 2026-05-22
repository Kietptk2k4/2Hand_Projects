package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrackShipmentResult(
        UUID shipmentId,
        UUID orderId,
        UUID sellerId,
        ShipmentAccessRole accessedAs,
        ShipmentStatus status,
        ShipmentCarrier carrier,
        ShipmentType shipmentType,
        String trackingNumber,
        String ghnOrderCode,
        Instant shippedAt,
        Instant deliveredAt,
        LocalDate estimatedDeliveryDate,
        OrderStatus orderStatus,
        boolean shipmentDelivered,
        boolean orderCompleted,
        List<ShipmentStatusHistoryEntry> timeline
) {
}
