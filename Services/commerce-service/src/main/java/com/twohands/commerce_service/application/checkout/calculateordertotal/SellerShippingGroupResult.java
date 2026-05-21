package com.twohands.commerce_service.application.checkout.calculateordertotal;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.util.UUID;

public record SellerShippingGroupResult(
        UUID sellerId,
        UUID shopId,
        BigDecimal shippingFee,
        ShipmentType shipmentType
) {
}
