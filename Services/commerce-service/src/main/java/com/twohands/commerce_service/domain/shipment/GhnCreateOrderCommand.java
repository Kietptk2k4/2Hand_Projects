package com.twohands.commerce_service.domain.shipment;

import java.util.UUID;

public record GhnCreateOrderCommand(
        UUID shipmentId,
        UUID orderId,
        int codAmount,
        int totalWeightGram,
        String receiverName,
        String receiverPhone,
        String toProvinceCode,
        String toDistrictCode,
        String toWardCode,
        String toAddressDetail,
        String fromProvinceCode,
        String fromDistrictCode,
        String fromWardCode,
        String fromAddressDetail
) {
}
