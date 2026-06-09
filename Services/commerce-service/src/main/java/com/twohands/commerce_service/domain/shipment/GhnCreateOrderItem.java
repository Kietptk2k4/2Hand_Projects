package com.twohands.commerce_service.domain.shipment;

public record GhnCreateOrderItem(
        String name,
        String code,
        int quantity,
        int priceVnd,
        int weightGram
) {
}
