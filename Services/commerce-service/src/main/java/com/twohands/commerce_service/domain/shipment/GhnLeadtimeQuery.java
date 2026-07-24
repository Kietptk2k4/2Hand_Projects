package com.twohands.commerce_service.domain.shipment;

public record GhnLeadtimeQuery(
        int fromDistrictId,
        String fromWardCode,
        int toDistrictId,
        String toWardCode,
        int serviceId
) {
}
