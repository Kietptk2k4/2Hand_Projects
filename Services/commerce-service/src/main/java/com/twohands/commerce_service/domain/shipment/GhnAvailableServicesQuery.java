package com.twohands.commerce_service.domain.shipment;

public record GhnAvailableServicesQuery(
        int fromDistrictId,
        int toDistrictId
) {
}
