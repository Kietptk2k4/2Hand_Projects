package com.twohands.auth_service.delivery.http.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 255, message = "Email max length is 255")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 32, message = "Password must be 8-32 characters")
        String password,

        @JsonProperty("confirm_password")
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}
