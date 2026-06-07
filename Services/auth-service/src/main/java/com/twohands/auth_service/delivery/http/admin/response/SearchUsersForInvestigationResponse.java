package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SearchUsersForInvestigationResponse(
		List<UserItem> users
) {
	public record UserItem(
			@JsonProperty("user_id")
			String userId,
			String email,
			@JsonProperty("display_name")
			String displayName,
			String status,
			@JsonProperty("role_codes")
			List<String> roleCodes
	) {
	}
}
