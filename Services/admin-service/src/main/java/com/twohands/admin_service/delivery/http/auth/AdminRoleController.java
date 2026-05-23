package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleCommand;
import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleResult;
import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleUseCase;
import com.twohands.admin_service.application.auth.viewadminroles.ViewAdminRolesResult;
import com.twohands.admin_service.application.auth.viewadminroles.ViewAdminRolesUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/me/roles")
public class AdminRoleController {

	private final ViewAdminRolesUseCase viewAdminRolesUseCase;
	private final CheckAdminRoleUseCase checkAdminRoleUseCase;

	public AdminRoleController(
			ViewAdminRolesUseCase viewAdminRolesUseCase,
			CheckAdminRoleUseCase checkAdminRoleUseCase
	) {
		this.viewAdminRolesUseCase = viewAdminRolesUseCase;
		this.checkAdminRoleUseCase = checkAdminRoleUseCase;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<ViewAdminRolesResponse>> listRoles() {
		ViewAdminRolesResult result = viewAdminRolesUseCase.execute();
		ViewAdminRolesResponse response = new ViewAdminRolesResponse(result.adminId(), result.roles());
		return ResponseEntity.ok(ApiResponse.success(200, viewAdminRolesUseCase.successMessage(), response));
	}

	@GetMapping("/check")
	public ResponseEntity<ApiResponse<CheckAdminRoleResponse>> checkRole(@RequestParam("role") String role) {
		CheckAdminRoleResult result = checkAdminRoleUseCase.execute(new CheckAdminRoleCommand(role));
		CheckAdminRoleResponse response = new CheckAdminRoleResponse(
				result.adminId(),
				result.roleCode(),
				result.granted()
		);
		return ResponseEntity.ok(ApiResponse.success(200, checkAdminRoleUseCase.successMessage(), response));
	}
}
