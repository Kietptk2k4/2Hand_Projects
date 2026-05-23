package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AdminLogoutRequest(
		@NotBlank
		@JsonProperty("refresh_token")
		String refreshToken
) {
}
