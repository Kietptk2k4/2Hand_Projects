package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ListSystemAnnouncementsResponse(
		int page,
		int size,

		@JsonProperty("total_elements")
		long totalElements,

		@JsonProperty("total_pages")
		int totalPages,

		List<SystemAnnouncementListEntryResponse> items
) {
}
