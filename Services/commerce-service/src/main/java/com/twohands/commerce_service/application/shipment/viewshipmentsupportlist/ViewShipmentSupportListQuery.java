package com.twohands.commerce_service.application.shipment.viewshipmentsupportlist;

public record ViewShipmentSupportListQuery(
        String status,
        String carrier,
        String sort,
        String q,
        String orderId,
        String from,
        String to,
        Integer page,
        Integer size
) {
}
