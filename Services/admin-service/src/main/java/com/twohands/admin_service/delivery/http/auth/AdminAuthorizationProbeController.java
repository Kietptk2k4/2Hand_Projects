package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal probe endpoint to verify permission enforcement (FR_AuthorizeAdminApi).
 */
@RestController
@RequestMapping("/admin/api/v1/authorization-probe")
public class AdminAuthorizationProbeController {

	@GetMapping("/user-suspend")
	@RequireAdminPermission(AdminPermission.USER_SUSPEND)
	public ResponseEntity<ApiResponse<Map<String, String>>> probeUserSuspendPermission() {
		return ResponseEntity.ok(ApiResponse.success(
				200,
				"Permission USER_SUSPEND granted",
				Map.of("permission", AdminPermission.USER_SUSPEND)
		));
	}
}
