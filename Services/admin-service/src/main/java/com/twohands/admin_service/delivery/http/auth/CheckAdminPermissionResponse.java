package com.twohands.admin_service.delivery.http.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CheckAdminPermissionResponse(
		@JsonProperty("admin_id")
		UUID adminId,
		@JsonProperty("permission_code")
		String permissionCode,
		boolean granted,
		@JsonProperty("resource_type")
		String resourceType,
		@JsonProperty("resource_id")
		String resourceId
) {
}
