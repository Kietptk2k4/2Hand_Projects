package com.twohands.commerce_service.domain.shipment;

public record GhnOrderInfoResult(
        String orderCode,
        String rawStatus,
        String rawResponse,
        boolean mock
) {
}
