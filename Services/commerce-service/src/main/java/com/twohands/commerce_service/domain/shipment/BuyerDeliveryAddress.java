package com.twohands.commerce_service.domain.shipment;

import java.util.UUID;

public record BuyerDeliveryAddress(
        UUID addressId,
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail
) {
    public String fullAddress() {
        return String.join(", ", addressDetail, wardCode, districtCode, provinceCode);
    }
}
