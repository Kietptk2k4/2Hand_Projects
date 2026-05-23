package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record SuspendUserRequest(
		@NotBlank
		@Size(max = 100)
		@JsonProperty("reason_code")
		String reasonCode,

		@NotBlank
		@Size(max = 4000)
		String description,

		@JsonProperty("expires_at")
		Instant expiresAt
) {
}
