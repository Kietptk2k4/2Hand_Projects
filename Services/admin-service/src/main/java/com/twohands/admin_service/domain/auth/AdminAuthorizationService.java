package com.twohands.admin_service.domain.auth;

import java.util.UUID;

/**
 * Permission checks for admin actions. MVP uses JWT claims; replace with Auth HTTP client when available.
 */
public interface AdminAuthorizationService {

	boolean hasPermission(UUID adminId, String permissionCode);

	boolean hasAnyRole(UUID adminId, String... roleCodes);
}
