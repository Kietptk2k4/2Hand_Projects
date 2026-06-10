package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminOverrideShipmentStatusRequest(
        String status,
        String reason,
        @JsonProperty("force") Boolean force
) {
    public boolean forceOrDefault() {
        return force != null && force;
    }
}
