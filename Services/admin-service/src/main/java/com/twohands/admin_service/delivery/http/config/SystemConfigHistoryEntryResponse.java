package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record SystemConfigHistoryEntryResponse(
		@JsonProperty("history_id")
		UUID historyId,

		@JsonProperty("config_key")
		String configKey,

		@JsonProperty("old_value")
		String oldValue,

		@JsonProperty("new_value")
		String newValue,

		@JsonProperty("changed_by")
		UUID changedBy,

		String reason,

		@JsonProperty("created_at")
		Instant createdAt,

		@JsonProperty("values_masked")
		boolean valuesMasked
) {
}
