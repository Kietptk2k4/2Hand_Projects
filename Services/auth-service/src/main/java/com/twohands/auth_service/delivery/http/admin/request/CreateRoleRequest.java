package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "Role code is required")
        @Size(min = 2, max = 32, message = "Role code must be between 2 and 32 characters")
        String code,

        @NotBlank(message = "Role name is required")
        @Size(min = 1, max = 100, message = "Role name must be between 1 and 100 characters")
        String name
) {
}
