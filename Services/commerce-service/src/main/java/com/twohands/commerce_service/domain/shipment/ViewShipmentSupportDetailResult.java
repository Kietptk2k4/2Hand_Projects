package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.util.List;
import java.util.UUID;

public record ViewShipmentSupportDetailResult(
        SellerShipmentRecord shipment,
        UUID buyerId,
        OrderStatus orderStatus,
        String carrierStatus,
        ShipmentAddressSnapshot shippingAddress,
        List<ShipmentOrderItemSummary> orderItems,
        List<ShipmentStatusHistoryEntry> statusHistory,
        List<GhnWebhookSummary> carrierWebhookEvents
) {
}
