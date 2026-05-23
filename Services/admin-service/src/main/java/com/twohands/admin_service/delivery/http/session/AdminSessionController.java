package com.twohands.admin_service.delivery.http.session;

import com.twohands.admin_service.application.session.revokeadminsession.RevokeAdminSessionCommand;
import com.twohands.admin_service.application.session.revokeadminsession.RevokeAdminSessionResult;
import com.twohands.admin_service.application.session.revokeadminsession.RevokeAdminSessionUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/admin-sessions")
public class AdminSessionController {

	private final RevokeAdminSessionUseCase revokeAdminSessionUseCase;

	public AdminSessionController(RevokeAdminSessionUseCase revokeAdminSessionUseCase) {
		this.revokeAdminSessionUseCase = revokeAdminSessionUseCase;
	}

	@PostMapping("/{sessionId}/revoke")
	@RequireAdminPermission(AdminPermission.ADMIN_SESSION_REVOKE)
	public ResponseEntity<ApiResponse<RevokeAdminSessionResponse>> revoke(
			@PathVariable UUID sessionId,
			@RequestBody(required = false) RevokeAdminSessionRequest request,
			HttpServletRequest httpServletRequest
	) {
		RevokeAdminSessionResult result = revokeAdminSessionUseCase.execute(new RevokeAdminSessionCommand(
				sessionId,
				request == null ? false : request.revokeAllSessionsOrDefault(),
				resolveBearerToken(httpServletRequest)
		));

		RevokeAdminSessionResponse data = new RevokeAdminSessionResponse(
				result.targetAdminUserId(),
				result.sessionId(),
				result.revokedSessionCount(),
				result.revokeAllSessions()
		);

		return ResponseEntity.ok(ApiResponse.success(200, revokeAdminSessionUseCase.successMessage(), data));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
