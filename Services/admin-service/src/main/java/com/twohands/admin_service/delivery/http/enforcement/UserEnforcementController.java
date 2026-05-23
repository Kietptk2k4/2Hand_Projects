package com.twohands.admin_service.delivery.http.enforcement;

import com.twohands.admin_service.application.enforcement.suspenduser.SuspendUserCommand;
import com.twohands.admin_service.application.enforcement.suspenduser.SuspendUserResult;
import com.twohands.admin_service.application.enforcement.suspenduser.SuspendUserUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/users")
public class UserEnforcementController {

	private final SuspendUserUseCase suspendUserUseCase;

	public UserEnforcementController(SuspendUserUseCase suspendUserUseCase) {
		this.suspendUserUseCase = suspendUserUseCase;
	}

	@PostMapping("/{userId}/suspend")
	@RequireAdminPermission(AdminPermission.USER_SUSPEND)
	public ResponseEntity<ApiResponse<SuspendUserResponse>> suspend(
			@PathVariable UUID userId,
			@Valid @RequestBody SuspendUserRequest request,
			HttpServletRequest httpServletRequest
	) {
		SuspendUserResult result = suspendUserUseCase.execute(new SuspendUserCommand(
				userId,
				request.reasonCode(),
				request.description(),
				request.expiresAt(),
				resolveBearerToken(httpServletRequest)
		));

		SuspendUserResponse data = new SuspendUserResponse(
				result.enforcementId(),
				result.userId(),
				result.reasonCode(),
				result.status().name(),
				result.expiresAt(),
				result.enforcedBy(),
				result.createdAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), suspendUserUseCase.successMessage(), data));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
