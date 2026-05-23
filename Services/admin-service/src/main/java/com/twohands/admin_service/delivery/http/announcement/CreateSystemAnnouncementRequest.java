package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSystemAnnouncementRequest(
		@NotBlank
		@Size(max = 500)
		String title,

		@NotBlank
		String content,

		@NotBlank
		String severity,

		@JsonProperty("is_pinned")
		Boolean pinned,

		Boolean dismissible
) {
}
