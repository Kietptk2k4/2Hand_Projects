package com.twohands.admin_service.application.auth.checkadminpermission;

import java.util.UUID;

public record CheckAdminPermissionResult(
		UUID adminId,
		String permissionCode,
		boolean granted,
		String resourceType,
		String resourceId
) {
}
