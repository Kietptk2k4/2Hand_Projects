package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ToggleSystemConfigResponse(
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

		@JsonProperty("updated_by")
		UUID updatedBy,

		@JsonProperty("updated_at")
		Instant updatedAt,

		@JsonProperty("history_id")
		UUID historyId,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId,

		@JsonProperty("state_changed")
		boolean stateChanged
) {
}
