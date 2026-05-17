package com.twohands.auth_service.delivery.http.users.request;

import jakarta.validation.constraints.NotBlank;

public record SoftDeleteAccountRequest(
        @NotBlank(message = "Password is required")
        String password
) {
}
