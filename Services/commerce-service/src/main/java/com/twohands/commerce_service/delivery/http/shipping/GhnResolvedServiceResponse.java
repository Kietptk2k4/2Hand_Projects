package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;

public record GhnResolvedServiceResponse(
        @JsonProperty("service_id") int serviceId,
        @JsonProperty("service_type_id") int serviceTypeId,
        @JsonProperty("short_name") String shortName,
        @JsonProperty("used_configured_default") boolean usedConfiguredDefault
) {
    public static GhnResolvedServiceResponse from(GhnResolvedService resolved) {
        return new GhnResolvedServiceResponse(
                resolved.serviceId(),
                resolved.serviceTypeId(),
                resolved.shortName(),
                resolved.usedConfiguredDefault()
        );
    }
}
