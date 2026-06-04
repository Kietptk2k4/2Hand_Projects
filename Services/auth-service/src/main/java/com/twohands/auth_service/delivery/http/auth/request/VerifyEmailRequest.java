package com.twohands.auth_service.delivery.http.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 512, message = "Token max length is 512")
        String token
) {
}
