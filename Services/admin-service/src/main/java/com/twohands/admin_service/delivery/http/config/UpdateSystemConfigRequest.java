package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSystemConfigRequest(
		@NotBlank
		@JsonProperty("config_value")
		String configValue,

		@Size(max = 4000)
		String description,

		@NotBlank
		@Size(max = 4000)
		String reason
) {
}
