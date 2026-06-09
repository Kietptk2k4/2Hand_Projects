package com.twohands.commerce_service.domain.shipment;

public record GhnShippingFeeQuery(
        int fromDistrictId,
        String fromWardCode,
        int toDistrictId,
        String toWardCode,
        int weightGram,
        int serviceId,
        int serviceTypeId,
        int lengthCm,
        int widthCm,
        int heightCm
) {
}
