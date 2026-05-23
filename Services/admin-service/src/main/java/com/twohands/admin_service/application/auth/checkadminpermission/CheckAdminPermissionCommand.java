package com.twohands.admin_service.application.auth.checkadminpermission;

public record CheckAdminPermissionCommand(
		String permissionCode,
		String resourceType,
		String resourceId
) {
}
