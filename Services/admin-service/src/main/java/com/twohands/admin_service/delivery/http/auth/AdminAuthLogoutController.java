package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.application.auth.adminlogout.AdminLogoutCommand;
import com.twohands.admin_service.application.auth.adminlogout.AdminLogoutUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/auth")
@ConditionalOnProperty(name = "admin.auth.login.gateway-enabled", havingValue = "true")
public class AdminAuthLogoutController {

	private final AdminLogoutUseCase adminLogoutUseCase;

	public AdminAuthLogoutController(AdminLogoutUseCase adminLogoutUseCase) {
		this.adminLogoutUseCase = adminLogoutUseCase;
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@Valid @RequestBody AdminLogoutRequest request,
			HttpServletRequest httpServletRequest
	) {
		adminLogoutUseCase.execute(new AdminLogoutCommand(
				request.refreshToken(),
				httpServletRequest.getRemoteAddr(),
				resolveBearerToken(httpServletRequest)
		));

		return ResponseEntity.ok(ApiResponse.success(200, adminLogoutUseCase.successMessage(), null));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
