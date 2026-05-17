package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AssignRolesToUsersRequest(
        @JsonProperty("role_id")
        @NotBlank(message = "Role ID is required")
        String roleId
) {
}
