package com.twohands.commerce_service.domain.shipment;

public record GhnDistrict(
        int districtId,
        int provinceId,
        String districtName,
        String code
) {
}
