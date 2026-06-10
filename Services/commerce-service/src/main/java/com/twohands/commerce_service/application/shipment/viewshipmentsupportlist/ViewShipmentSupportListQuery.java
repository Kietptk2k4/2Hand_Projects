package com.twohands.commerce_service.application.shipment.viewshipmentsupportlist;

public record ViewShipmentSupportListQuery(
        String status,
        String carrier,
        String sort,
        Integer page,
        Integer size
) {
}
