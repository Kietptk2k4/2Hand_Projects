package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CheckAdminRoleResponse(
		@JsonProperty("admin_id")
		UUID adminId,
		@JsonProperty("role_code")
		String roleCode,
		boolean granted
) {
}
