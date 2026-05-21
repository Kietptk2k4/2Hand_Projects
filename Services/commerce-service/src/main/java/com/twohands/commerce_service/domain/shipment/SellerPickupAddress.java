package com.twohands.commerce_service.domain.shipment;

import java.util.UUID;

public record SellerPickupAddress(
        UUID shopId,
        UUID sellerId,
        String pickupName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail
) {
}
