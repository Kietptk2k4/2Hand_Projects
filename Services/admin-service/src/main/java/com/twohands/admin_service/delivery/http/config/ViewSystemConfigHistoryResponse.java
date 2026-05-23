package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewSystemConfigHistoryResponse(
		@JsonProperty("config_id")
		UUID configId,

		@JsonProperty("config_key")
		String configKey,

		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		@JsonProperty("values_masked")
		boolean valuesMasked,

		List<SystemConfigHistoryEntryResponse> history
) {
}
