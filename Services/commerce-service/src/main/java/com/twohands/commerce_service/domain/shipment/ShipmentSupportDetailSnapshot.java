package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.util.List;
import java.util.UUID;

public record ShipmentSupportDetailSnapshot(
        SellerShipmentRecord shipment,
        UUID buyerId,
        OrderStatus orderStatus,
        ShipmentAddressSnapshot shippingAddress,
        List<ShipmentOrderItemSummary> orderItems,
        List<ShipmentStatusHistoryEntry> statusHistory,
        List<GhnWebhookSummary> carrierWebhookEvents
) {
}
