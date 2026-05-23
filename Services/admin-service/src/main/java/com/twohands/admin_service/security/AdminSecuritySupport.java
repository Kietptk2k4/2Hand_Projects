package com.twohands.admin_service.security;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminSecuritySupport {

	private AdminSecuritySupport() {
	}

	public static AuthenticatedUser requireAuthenticatedAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
		return user;
	}

	public static String resolveBearerToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getDetails() == null) {
			return null;
		}
		if (authentication.getDetails() instanceof BearerTokenDetails details) {
			return details.token();
		}
		return null;
	}
}
