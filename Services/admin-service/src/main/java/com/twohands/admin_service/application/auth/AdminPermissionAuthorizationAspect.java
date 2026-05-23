package com.twohands.admin_service.application.auth;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminPermissionAuthorizationAspect {

	private final AdminAuthorizationService adminAuthorizationService;

	public AdminPermissionAuthorizationAspect(AdminAuthorizationService adminAuthorizationService) {
		this.adminAuthorizationService = adminAuthorizationService;
	}

	@Before("@annotation(requireAdminPermission)")
	public void authorize(RequireAdminPermission requireAdminPermission) {
		adminAuthorizationService.requireAnyPermission(requireAdminPermission.value());
	}
}
