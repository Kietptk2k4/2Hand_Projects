package com.twohands.admin_service.application.auth.viewadminroles;

import java.util.List;
import java.util.UUID;

public record ViewAdminRolesResult(
		UUID adminId,
		List<String> roles
) {
}
