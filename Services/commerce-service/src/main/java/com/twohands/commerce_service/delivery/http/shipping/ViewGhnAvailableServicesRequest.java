package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ViewGhnAvailableServicesRequest(
        @JsonProperty("from_district_id")
        @NotNull
        @Positive
        Integer fromDistrictId,

        @JsonProperty("to_district_id")
        @NotNull
        @Positive
        Integer toDistrictId
) {
}
