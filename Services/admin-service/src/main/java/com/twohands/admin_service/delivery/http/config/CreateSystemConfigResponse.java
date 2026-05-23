package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CreateSystemConfigResponse(
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

		@JsonProperty("history_id")
		UUID historyId,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
