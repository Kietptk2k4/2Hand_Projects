package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionCommand;
import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionResult;
import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/me/permissions")
public class AdminPermissionCheckController {

	private final CheckAdminPermissionUseCase checkAdminPermissionUseCase;

	public AdminPermissionCheckController(CheckAdminPermissionUseCase checkAdminPermissionUseCase) {
		this.checkAdminPermissionUseCase = checkAdminPermissionUseCase;
	}

	@GetMapping("/check")
	public ResponseEntity<ApiResponse<CheckAdminPermissionResponse>> checkPermission(
			@RequestParam("permission") String permission,
			@RequestParam(value = "resource_type", required = false) String resourceType,
			@RequestParam(value = "resource_id", required = false) String resourceId
	) {
		CheckAdminPermissionResult result = checkAdminPermissionUseCase.execute(
				new CheckAdminPermissionCommand(permission, resourceType, resourceId)
		);
		CheckAdminPermissionResponse response = new CheckAdminPermissionResponse(
				result.adminId(),
				result.permissionCode(),
				result.granted(),
				result.resourceType(),
				result.resourceId()
		);
		return ResponseEntity.ok(ApiResponse.success(200, checkAdminPermissionUseCase.successMessage(), response));
	}
}
