package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PublishSystemAnnouncementRequest(
		@JsonProperty("recipient_user_ids") List<UUID> recipientUserIds,
		@JsonProperty("target_audience") String targetAudience
) {
}
