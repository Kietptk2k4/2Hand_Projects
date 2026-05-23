package com.twohands.admin_service.delivery.http.enforcement;

import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementCommand;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementResult;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementUseCase;
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
@RequestMapping("/admin/api/v1/user-enforcements")
public class UserEnforcementRevokeController {

	private final RevokeUserEnforcementUseCase revokeUserEnforcementUseCase;

	public UserEnforcementRevokeController(RevokeUserEnforcementUseCase revokeUserEnforcementUseCase) {
		this.revokeUserEnforcementUseCase = revokeUserEnforcementUseCase;
	}

	@PostMapping("/{enforcementId}/revoke")
	@RequireAdminPermission(AdminPermission.USER_ENFORCEMENT_REVOKE)
	public ResponseEntity<ApiResponse<RevokeUserEnforcementResponse>> revoke(
			@PathVariable UUID enforcementId,
			@Valid @RequestBody(required = false) RevokeUserEnforcementRequest request,
			HttpServletRequest httpServletRequest
	) {
		RevokeUserEnforcementRequest body = request == null ? new RevokeUserEnforcementRequest(null, null) : request;

		RevokeUserEnforcementResult result = revokeUserEnforcementUseCase.execute(new RevokeUserEnforcementCommand(
				enforcementId,
				body.note(),
				body.reason(),
				resolveBearerToken(httpServletRequest)
		));

		RevokeUserEnforcementResponse data = new RevokeUserEnforcementResponse(
				result.enforcementId(),
				result.userId(),
				result.actionType().name(),
				result.status().name(),
				result.revokedBy(),
				result.updatedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), revokeUserEnforcementUseCase.successMessage(), data));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
