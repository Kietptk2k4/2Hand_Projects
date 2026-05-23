package com.twohands.admin_service.domain.auth;

import java.util.UUID;

/**
 * Optional Auth Service permission lookup when JWT claims are insufficient.
 */
public interface AuthPermissionGateway {

	boolean isEnabled();

	boolean isAvailable();

	boolean hasPermission(UUID adminId, String permissionCode, String bearerToken);
}
