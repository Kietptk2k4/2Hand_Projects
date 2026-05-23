package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewAdminRolesResponse(
		@JsonProperty("admin_id")
		UUID adminId,
		List<String> roles
) {
}
