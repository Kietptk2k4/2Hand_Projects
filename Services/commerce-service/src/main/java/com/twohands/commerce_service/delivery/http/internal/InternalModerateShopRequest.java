package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InternalModerateShopRequest(
        @NotNull @JsonProperty("moderated_by_admin_id") UUID moderatedByAdminId,
        @NotBlank String action,
        @NotBlank String reason
) {
}
