package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record SystemAnnouncementListEntryResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String content,
		String severity,
		String status,

		@JsonProperty("is_pinned")
		boolean pinned,

		boolean dismissible,

		@JsonProperty("created_by")
		UUID createdBy,

		@JsonProperty("created_at")
		Instant createdAt,

		@JsonProperty("sent_at")
		Instant sentAt
) {
}
