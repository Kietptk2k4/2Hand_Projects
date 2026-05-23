package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record PinSystemAnnouncementRequest(
		@NotNull
		@JsonProperty("is_pinned")
		Boolean pinned
) {
}
