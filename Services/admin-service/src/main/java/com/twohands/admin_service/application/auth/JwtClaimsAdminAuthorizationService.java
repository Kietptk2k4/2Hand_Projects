package com.twohands.admin_service.application.auth;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class JwtClaimsAdminAuthorizationService implements AdminAuthorizationService {

	@Override
	public boolean hasPermission(UUID adminId, String permissionCode) {
		AuthenticatedUser user = requireAuthenticatedUser(adminId);
		return user.permissions().contains(permissionCode);
	}

	@Override
	public boolean hasAnyRole(UUID adminId, String... roleCodes) {
		AuthenticatedUser user = requireAuthenticatedUser(adminId);
		return Arrays.stream(roleCodes).anyMatch(code -> user.roles().contains(code));
	}

	private AuthenticatedUser requireAuthenticatedUser(UUID adminId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
		if (!user.userId().equals(adminId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Admin identity mismatch");
		}
		return user;
	}
}
