package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RevokeUserEnforcementByAdminResponse(
        @JsonProperty("user_id")
        UUID userId,
        String status,
        boolean reactivated
) {
}
