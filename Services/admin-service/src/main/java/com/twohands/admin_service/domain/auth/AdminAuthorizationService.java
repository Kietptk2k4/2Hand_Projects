package com.twohands.admin_service.domain.auth;

import com.twohands.admin_service.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

/**
 * Authentication and permission checks for admin APIs (FR_AuthorizeAdminApi).
 */
public interface AdminAuthorizationService {

	AuthenticatedUser requireCurrentAdmin();

	UUID requireCurrentAdminId();

	void requirePermission(String permissionCode);

	void requireAnyPermission(String... permissionCodes);

	void requireAnyRole(String... roleCodes);

	boolean hasPermission(UUID adminId, String permissionCode);

	boolean hasRole(UUID adminId, String roleCode);

	boolean hasAnyRole(UUID adminId, String... roleCodes);

	List<String> getRoles(UUID adminId);
}
