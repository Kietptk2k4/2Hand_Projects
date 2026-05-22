package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateShopVacationRequest(
        @JsonProperty("is_vacation")
        @NotNull(message = "is_vacation is required")
        Boolean isVacation,

        @JsonProperty("vacation_message")
        @Size(max = 500, message = "vacation_message must be at most 500 characters")
        String vacationMessage
) {
}
