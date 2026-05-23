package com.twohands.admin_service.delivery.http.enforcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewCurrentUserEnforcementResponse(
		@JsonProperty("user_id")
		UUID userId,
		List<CurrentUserEnforcementResponse> enforcements
) {
}
