package com.twohands.admin_service.delivery.http.auth;

import com.twohands.admin_service.application.auth.adminlogin.AdminLoginCommand;
import com.twohands.admin_service.application.auth.adminlogin.AdminLoginResult;
import com.twohands.admin_service.application.auth.adminlogin.AdminLoginUseCase;
import com.twohands.admin_service.application.auth.refreshadmintoken.RefreshAdminTokenCommand;
import com.twohands.admin_service.application.auth.refreshadmintoken.RefreshAdminTokenResult;
import com.twohands.admin_service.application.auth.refreshadmintoken.RefreshAdminTokenUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/auth")
@ConditionalOnProperty(name = "admin.auth.login.gateway-enabled", havingValue = "true")
public class AdminAuthLoginController {

	private final AdminLoginUseCase adminLoginUseCase;
	private final RefreshAdminTokenUseCase refreshAdminTokenUseCase;

	public AdminAuthLoginController(
			AdminLoginUseCase adminLoginUseCase,
			RefreshAdminTokenUseCase refreshAdminTokenUseCase
	) {
		this.adminLoginUseCase = adminLoginUseCase;
		this.refreshAdminTokenUseCase = refreshAdminTokenUseCase;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
			@Valid @RequestBody AdminLoginRequest request,
			@RequestHeader(name = "X-Device-Id", required = false) String deviceId,
			HttpServletRequest httpServletRequest
	) {
		AdminLoginResult result = adminLoginUseCase.execute(new AdminLoginCommand(
				request.email(),
				request.password(),
				httpServletRequest.getRemoteAddr(),
				httpServletRequest.getHeader("User-Agent"),
				deviceId
		));

		AdminLoginResponse data = new AdminLoginResponse(
				result.accessToken(),
				result.refreshToken(),
				result.expiresIn(),
				new AdminLoginResponse.AdminUserInfo(
						result.adminId().toString(),
						result.email(),
						result.status()
				),
				result.roles(),
				result.permissions()
		);

		return ResponseEntity.ok(ApiResponse.success(200, adminLoginUseCase.successMessage(), data));
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<ApiResponse<RefreshAdminTokenResponse>> refresh(
			@Valid @RequestBody RefreshAdminTokenRequest request,
			HttpServletRequest httpServletRequest
	) {
		RefreshAdminTokenResult result = refreshAdminTokenUseCase.execute(new RefreshAdminTokenCommand(
				request.refreshToken(),
				httpServletRequest.getRemoteAddr()
		));

		RefreshAdminTokenResponse data = new RefreshAdminTokenResponse(
				result.accessToken(),
				result.expiresIn(),
				new AdminLoginResponse.AdminUserInfo(
						result.adminId().toString(),
						result.email(),
						result.status()
				),
				result.roles(),
				result.permissions()
		);

		return ResponseEntity.ok(ApiResponse.success(200, refreshAdminTokenUseCase.successMessage(), data));
	}
}
