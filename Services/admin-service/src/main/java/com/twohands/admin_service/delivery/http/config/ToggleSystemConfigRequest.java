package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ToggleSystemConfigRequest(
		@NotNull
		@JsonProperty("is_active")
		Boolean active,

		@NotBlank
		@Size(max = 4000)
		String reason
) {
}
