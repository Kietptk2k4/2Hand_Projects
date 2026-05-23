package com.twohands.admin_service.delivery.http.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password
) {
}
