package com.twohands.admin_service.application.auth.checkadminrole;

import java.util.UUID;

public record CheckAdminRoleResult(
		UUID adminId,
		String roleCode,
		boolean granted
) {
}
