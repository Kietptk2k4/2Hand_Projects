package com.twohands.commerce_service.domain.shipment;

import java.util.List;
import java.util.UUID;

public record GhnCreateOrderCommand(
        UUID shipmentId,
        UUID orderId,
        int codAmount,
        int totalWeightGram,
        int serviceId,
        int serviceTypeId,
        int lengthCm,
        int widthCm,
        int heightCm,
        String receiverName,
        String receiverPhone,
        String toDistrictCode,
        String toWardCode,
        String toAddressDetail,
        String fromName,
        String fromPhone,
        String fromDistrictCode,
        String fromWardCode,
        String fromAddressDetail,
        String content,
        List<GhnCreateOrderItem> items
) {
}
