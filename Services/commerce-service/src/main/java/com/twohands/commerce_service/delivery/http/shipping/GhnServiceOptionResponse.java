package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;

public record GhnServiceOptionResponse(
        @JsonProperty("service_id") int serviceId,
        @JsonProperty("service_type_id") int serviceTypeId,
        @JsonProperty("short_name") String shortName
) {
    public static GhnServiceOptionResponse from(GhnServiceOption option) {
        return new GhnServiceOptionResponse(
                option.serviceId(),
                option.serviceTypeId(),
                option.shortName()
        );
    }
}
