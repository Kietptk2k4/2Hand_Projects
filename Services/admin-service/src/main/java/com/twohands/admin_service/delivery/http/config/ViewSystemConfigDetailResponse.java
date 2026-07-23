package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ViewSystemConfigDetailResponse(
		@JsonProperty("config_id")
		UUID configId,

		@JsonProperty("config_key")
		String configKey,

		@JsonProperty("config_value")
		String configValue,

		@JsonProperty("value_type")
		String valueType,

		String description,

		@JsonProperty("is_active")
		boolean active,

		@JsonProperty("created_by")
		UUID createdBy,

		@JsonProperty("created_at")
		Instant createdAt,

		@JsonProperty("updated_by")
		UUID updatedBy,

		@JsonProperty("updated_at")
		Instant updatedAt,

		@JsonProperty("value_masked")
		boolean valueMasked
) {
}
