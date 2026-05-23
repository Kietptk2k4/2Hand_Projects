package com.twohands.admin_service.application.auth.checkadminrole;

import com.twohands.admin_service.constant.AdminRole;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CheckAdminRoleUseCase {

	private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,63}$");
	private static final String SUCCESS_MESSAGE = "Kiem tra role admin thanh cong.";

	private final AdminAuthorizationService adminAuthorizationService;

	public CheckAdminRoleUseCase(AdminAuthorizationService adminAuthorizationService) {
		this.adminAuthorizationService = adminAuthorizationService;
	}

	public CheckAdminRoleResult execute(CheckAdminRoleCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		String roleCode = validateRoleCode(command.roleCode());
		boolean granted = adminAuthorizationService.hasRole(adminId, roleCode);
		return new CheckAdminRoleResult(adminId, roleCode, granted);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private String validateRoleCode(String roleCode) {
		if (roleCode == null || roleCode.isBlank()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"role is required",
					"role",
					"must not be blank"
			);
		}
		String normalized = roleCode.trim().toUpperCase();
		if (!ROLE_CODE_PATTERN.matcher(normalized).matches()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Invalid role code format",
					"role",
					"must match UPPER_SNAKE_CASE"
			);
		}
		if (!AdminRole.isKnown(normalized)) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Unknown admin role code",
					"role",
					"must be MODERATOR, SUPPORT, or SUPER_ADMIN"
			);
		}
		return normalized;
	}
}
