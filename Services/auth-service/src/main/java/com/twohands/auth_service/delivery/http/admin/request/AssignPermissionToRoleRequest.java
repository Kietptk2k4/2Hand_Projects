package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AssignPermissionToRoleRequest(
        @JsonProperty("permission_code")
        @NotBlank(message = "Permission code is required")
        String permissionCode
) {
}
