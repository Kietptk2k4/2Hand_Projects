package com.twohands.commerce_service.delivery.http.checkout;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.util.UUID;

public record SellerShippingGroupResponse(
        UUID sellerId,
        UUID shopId,
        BigDecimal shippingFee,
        ShipmentType shipmentType
) {
}
