package com.twohands.commerce_service.domain.shipping;

import java.util.UUID;

public record SellerShippingProfile(
        UUID shopId,
        UUID sellerId,
        String provinceCode,
        String districtCode,
        String wardCode
) {
}
