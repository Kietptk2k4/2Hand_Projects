package com.twohands.commerce_service.domain.shipment;

import java.time.LocalDate;

public record GhnCreateOrderResult(
        String ghnOrderCode,
        String ghnShopId,
        String trackingNumber,
        String providerResponseJson,
        boolean mockProvider,
        LocalDate expectedDeliveryDate
) {
}
