package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateShipmentDraft(
        UUID shipmentId,
        UUID orderId,
        UUID sellerId,
        ShipmentCarrier carrier,
        ShipmentType shipmentType,
        int totalWeightGram,
        BigDecimal shippingFee,
        BigDecimal codAmount,
        LocalDate estimatedDeliveryDate,
        String trackingNumber,
        List<UUID> orderItemIds,
        BuyerDeliveryAddress deliveryAddress
) {
}
