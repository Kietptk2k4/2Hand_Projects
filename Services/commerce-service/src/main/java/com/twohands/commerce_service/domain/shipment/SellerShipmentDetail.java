package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;

import java.util.List;

public record SellerShipmentDetail(
        SellerShipmentRecord shipment,
        ShipmentAddressSnapshot addressSnapshot,
        List<ShipmentOrderItemSummary> orderItems,
        CommerceBuyerSummary buyer
) {
}
