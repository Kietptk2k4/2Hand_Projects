package com.twohands.commerce_service.domain.shipment;

public record GhnCreateOrderResult(
        String ghnOrderCode,
        String ghnShopId,
        String trackingNumber,
        String providerResponseJson,
        boolean mockProvider
) {
}
