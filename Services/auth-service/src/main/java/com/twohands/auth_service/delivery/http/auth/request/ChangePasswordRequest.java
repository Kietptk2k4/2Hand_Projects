package com.twohands.auth_service.delivery.http.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @JsonProperty("current_password")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @JsonProperty("new_password")
        @NotBlank(message = "New password is required")
        String newPassword,

        @JsonProperty("confirm_new_password")
        @NotBlank(message = "Confirm new password is required")
        String confirmNewPassword
) {
}
