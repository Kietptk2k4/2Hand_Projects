package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CheckUserPermissionResponse(
        @JsonProperty("user_id")
        String userId,
        List<PermissionData> permissions
) {
    public record PermissionData(
            String code
    ) {
    }
}
