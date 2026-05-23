package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record PublishSystemAnnouncementResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String severity,
		String status,

		@JsonProperty("is_pinned")
		boolean pinned,

		boolean dismissible,

		@JsonProperty("sent_at")
		Instant sentAt,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
