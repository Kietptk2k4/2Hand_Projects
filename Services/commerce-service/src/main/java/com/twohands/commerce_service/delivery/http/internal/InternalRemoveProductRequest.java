package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InternalRemoveProductRequest(
        @NotNull @JsonProperty("removed_by_admin_id") UUID removedByAdminId,
        @NotBlank String reason
) {
}
