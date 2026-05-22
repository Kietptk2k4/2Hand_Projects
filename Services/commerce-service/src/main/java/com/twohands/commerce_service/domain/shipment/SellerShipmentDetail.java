package com.twohands.commerce_service.domain.shipment;

import java.util.List;

public record SellerShipmentDetail(
        SellerShipmentRecord shipment,
        ShipmentAddressSnapshot addressSnapshot,
        List<ShipmentOrderItemSummary> orderItems
) {
}
