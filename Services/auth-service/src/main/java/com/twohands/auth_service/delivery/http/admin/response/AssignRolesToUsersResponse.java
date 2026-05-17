package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AssignRolesToUsersResponse(
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("role_id")
        String roleId
) {
}
