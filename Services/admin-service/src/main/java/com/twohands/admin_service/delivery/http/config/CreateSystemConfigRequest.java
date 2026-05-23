package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSystemConfigRequest(
		@NotBlank
		@Size(max = 255)
		@JsonProperty("config_key")
		String configKey,

		@NotBlank
		@JsonProperty("config_value")
		String configValue,

		@NotBlank
		@JsonProperty("value_type")
		String valueType,

		@Size(max = 4000)
		String description,

		@JsonProperty("is_active")
		Boolean active,

		@NotBlank
		@Size(max = 4000)
		String reason
) {
}
