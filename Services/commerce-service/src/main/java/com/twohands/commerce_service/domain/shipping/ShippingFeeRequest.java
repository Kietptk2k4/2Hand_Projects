package com.twohands.commerce_service.domain.shipping;

import java.math.BigDecimal;

public record ShippingFeeRequest(
        SellerShippingProfile pickup,
        String destinationProvinceCode,
        String destinationDistrictCode,
        int totalWeightGram,
        ShipmentType shipmentType
) {
}
