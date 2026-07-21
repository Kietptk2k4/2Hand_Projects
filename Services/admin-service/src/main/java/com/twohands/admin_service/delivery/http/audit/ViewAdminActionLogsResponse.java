package com.twohands.admin_service.delivery.http.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ViewAdminActionLogsResponse(
		int page,
		int size,
		@JsonProperty("total_elements")
		long totalElements,
		@JsonProperty("total_pages")
		int totalPages,
		List<AdminActionLogEntryResponse> logs
) {
}
