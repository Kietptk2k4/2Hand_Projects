package com.twohands.admin_service.application.auth;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.auth.AuthPermissionGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.AdminSecuritySupport;
import com.twohands.admin_service.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class JwtClaimsAdminAuthorizationService implements AdminAuthorizationService {

	private final AuthPermissionGateway authPermissionGateway;

	public JwtClaimsAdminAuthorizationService(AuthPermissionGateway authPermissionGateway) {
		this.authPermissionGateway = authPermissionGateway;
	}

	@Override
	public AuthenticatedUser requireCurrentAdmin() {
		return AdminSecuritySupport.requireAuthenticatedAdmin();
	}

	@Override
	public UUID requireCurrentAdminId() {
		return requireCurrentAdmin().userId();
	}

	@Override
	public void requirePermission(String permissionCode) {
		AuthenticatedUser admin = requireCurrentAdmin();
		if (!hasPermission(admin.userId(), permissionCode)) {
			failMissingPermission(permissionCode);
		}
	}

	@Override
	public void requireAnyPermission(String... permissionCodes) {
		AuthenticatedUser admin = requireCurrentAdmin();
		for (String permissionCode : permissionCodes) {
			if (hasPermission(admin.userId(), permissionCode)) {
				return;
			}
		}
		failMissingPermission(String.join(", ", permissionCodes));
	}

	@Override
	public void requireAnyRole(String... roleCodes) {
		AuthenticatedUser admin = requireCurrentAdmin();
		if (!hasAnyRole(admin.userId(), roleCodes)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Missing required admin role");
		}
	}

	@Override
	public boolean hasPermission(UUID adminId, String permissionCode) {
		AuthenticatedUser admin = requireMatchingAdmin(adminId);
		if (contains(admin.permissions(), permissionCode)) {
			return true;
		}
		return resolvePermissionFromAuth(adminId, permissionCode);
	}

	@Override
	public boolean hasAnyRole(UUID adminId, String... roleCodes) {
		AuthenticatedUser admin = requireMatchingAdmin(adminId);
		return Arrays.stream(roleCodes).anyMatch(code -> contains(admin.roles(), code));
	}

	private boolean resolvePermissionFromAuth(UUID adminId, String permissionCode) {
		if (!authPermissionGateway.isEnabled()) {
			return false;
		}
		if (!authPermissionGateway.isAvailable()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Cannot verify admin permission; authorization service unavailable"
			);
		}
		String bearerToken = AdminSecuritySupport.resolveBearerToken();
		if (bearerToken == null) {
			return false;
		}
		return authPermissionGateway.hasPermission(adminId, permissionCode, bearerToken);
	}

	private AuthenticatedUser requireMatchingAdmin(UUID adminId) {
		AuthenticatedUser admin = AdminSecuritySupport.requireAuthenticatedAdmin();
		if (!admin.userId().equals(adminId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Admin identity mismatch");
		}
		return admin;
	}

	private void failMissingPermission(String permissionCode) {
		throw new AppException(ErrorCode.FORBIDDEN, "Missing permission: " + permissionCode);
	}

	private boolean contains(java.util.List<String> values, String expected) {
		return values != null && values.stream().anyMatch(expected::equals);
	}
}
