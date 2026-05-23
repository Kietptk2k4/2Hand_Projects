package com.twohands.admin_service.application.auth.checkadminpermission;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CheckAdminPermissionUseCase {

	private static final Pattern PERMISSION_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,63}$");
	private static final String SUCCESS_MESSAGE = "Kiem tra permission admin thanh cong.";

	private final AdminAuthorizationService adminAuthorizationService;

	public CheckAdminPermissionUseCase(AdminAuthorizationService adminAuthorizationService) {
		this.adminAuthorizationService = adminAuthorizationService;
	}

	public CheckAdminPermissionResult execute(CheckAdminPermissionCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		String permissionCode = validatePermissionCode(command.permissionCode());
		boolean granted = adminAuthorizationService.hasPermission(adminId, permissionCode);
		return new CheckAdminPermissionResult(
				adminId,
				permissionCode,
				granted,
				normalizeOptional(command.resourceType()),
				normalizeOptional(command.resourceId())
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private String validatePermissionCode(String permissionCode) {
		if (permissionCode == null || permissionCode.isBlank()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"permission is required",
					"permission",
					"must not be blank"
			);
		}
		String normalized = permissionCode.trim().toUpperCase();
		if (!PERMISSION_CODE_PATTERN.matcher(normalized).matches()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Invalid permission code format",
					"permission",
					"must match UPPER_SNAKE_CASE"
			);
		}
		if (!AdminPermission.isKnown(normalized)) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Unknown permission code",
					"permission",
					"must be a supported admin permission"
			);
		}
		return normalized;
	}

	private String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
