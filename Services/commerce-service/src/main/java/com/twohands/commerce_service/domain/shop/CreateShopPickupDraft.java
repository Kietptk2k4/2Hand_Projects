package com.twohands.commerce_service.domain.shop;

public record CreateShopPickupDraft(
        String pickupName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail
) {
}
