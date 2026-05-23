package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CreateSystemAnnouncementResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String content,
		String severity,

		@JsonProperty("is_pinned")
		boolean pinned,

		boolean dismissible,
		String status,

		@JsonProperty("created_by")
		UUID createdBy,

		@JsonProperty("created_at")
		Instant createdAt
) {
}
