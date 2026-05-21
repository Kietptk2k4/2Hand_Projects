package com.twohands.commerce_service.application.shipping;

import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.util.List;

public record CartShippingQuoteContext(
        UserAddress destinationAddress,
        ShipmentType shipmentType,
        List<SellerWeightGroup> sellerGroups
) {
}
