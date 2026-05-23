package com.twohands.admin_service.application.auth.viewadminroles;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ViewAdminRolesUseCase {

	private static final String SUCCESS_MESSAGE = "Lay danh sach role admin thanh cong.";

	private final AdminAuthorizationService adminAuthorizationService;

	public ViewAdminRolesUseCase(AdminAuthorizationService adminAuthorizationService) {
		this.adminAuthorizationService = adminAuthorizationService;
	}

	public ViewAdminRolesResult execute() {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		return new ViewAdminRolesResult(adminId, adminAuthorizationService.getRoles(adminId));
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
