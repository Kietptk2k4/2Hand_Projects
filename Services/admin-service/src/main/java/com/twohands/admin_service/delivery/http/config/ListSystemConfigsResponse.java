package com.twohands.admin_service.delivery.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ListSystemConfigsResponse(
		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		List<SystemConfigListEntryResponse> items
) {
}
