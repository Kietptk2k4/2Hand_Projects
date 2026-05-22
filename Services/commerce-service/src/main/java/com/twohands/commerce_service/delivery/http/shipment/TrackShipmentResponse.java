package com.twohands.commerce_service.delivery.http.shipment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrackShipmentResponse(
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
        List<ShipmentStatusTimelineEntryResponse> timeline
) {
    public record ShipmentStatusTimelineEntryResponse(
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String rawStatus,
            Instant occurredAt
    ) {
    }
}
