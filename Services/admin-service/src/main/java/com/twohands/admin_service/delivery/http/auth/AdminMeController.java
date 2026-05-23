package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/me")
public class AdminMeController {

	private final AdminAuthorizationService adminAuthorizationService;

	public AdminMeController(AdminAuthorizationService adminAuthorizationService) {
		this.adminAuthorizationService = adminAuthorizationService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<AdminMeResponse>> me() {
		AuthenticatedUser admin = adminAuthorizationService.requireCurrentAdmin();
		AdminMeResponse data = new AdminMeResponse(admin.userId(), admin.roles(), admin.permissions());
		return ResponseEntity.ok(ApiResponse.success(200, "Authenticated admin profile", data));
	}
}
