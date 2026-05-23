package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SuspendUserByAdminRequest(
        @JsonProperty("enforcement_id")
        @NotNull(message = "enforcement_id is required")
        UUID enforcementId,
        @JsonProperty("reason_code")
        @NotBlank(message = "reason_code is required")
        String reasonCode,
        @NotBlank(message = "description is required")
        String description,
        @JsonProperty("expires_at")
        Instant expiresAt
) {
}
