package com.twohands.commerce_service.domain.order;

public record ShippingAddressSnapshot(
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        String fullAddress
) {
}
