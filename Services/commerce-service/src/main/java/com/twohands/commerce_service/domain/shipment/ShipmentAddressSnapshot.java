package com.twohands.commerce_service.domain.shipment;

public record ShipmentAddressSnapshot(
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        String fullAddress
) {
}
