package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RevokeUserEnforcementByAdminRequest(
        @JsonProperty("user_id")
        UUID userId,
        @JsonProperty("action_type")
        String actionType,
        @JsonProperty("reactivate_user")
        boolean reactivateUser,
        String note,
        String reason
) {
}
