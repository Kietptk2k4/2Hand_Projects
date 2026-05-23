package com.twohands.admin_service.delivery.http.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestorePostRequest(
		@NotBlank
		@Size(max = 4000)
		String reason,

		@Size(max = 4000)
		String note
) {
}
