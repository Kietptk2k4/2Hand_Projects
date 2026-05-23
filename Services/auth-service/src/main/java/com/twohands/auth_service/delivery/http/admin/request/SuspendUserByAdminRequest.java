package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record SuspendUserByAdminRequest(
        @JsonProperty("enforcement_id")
        UUID enforcementId,
        @JsonProperty("reason_code")
        String reasonCode,
        String description,
        @JsonProperty("expires_at")
        Instant expiresAt
) {
}
