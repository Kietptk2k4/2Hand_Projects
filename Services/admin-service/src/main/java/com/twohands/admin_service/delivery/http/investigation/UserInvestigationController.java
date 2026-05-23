package com.twohands.admin_service.delivery.http.investigation;

import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationQuery;
import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationResult;
import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/users")
public class UserInvestigationController {

	private final ViewUserProfileForInvestigationUseCase viewUserProfileForInvestigationUseCase;

	public UserInvestigationController(ViewUserProfileForInvestigationUseCase viewUserProfileForInvestigationUseCase) {
		this.viewUserProfileForInvestigationUseCase = viewUserProfileForInvestigationUseCase;
	}

	@GetMapping("/{userId}/profile")
	@RequireAdminPermission(AdminPermission.USER_INVESTIGATION_READ)
	public ResponseEntity<ApiResponse<ViewUserProfileForInvestigationResponse>> viewProfile(
			@PathVariable UUID userId,
			HttpServletRequest httpServletRequest
	) {
		ViewUserProfileForInvestigationResult result = viewUserProfileForInvestigationUseCase.execute(
				new ViewUserProfileForInvestigationQuery(userId, resolveBearerToken(httpServletRequest))
		);

		ViewUserProfileForInvestigationResponse data = new ViewUserProfileForInvestigationResponse(
				result.userId(),
				result.email(),
				result.status(),
				result.emailVerified(),
				result.phoneVerified(),
				result.lastLoginAt(),
				result.createdAt(),
				result.displayName(),
				result.avatarUrl(),
				result.bio(),
				result.website(),
				result.isPrivate(),
				result.currentEnforcements().stream()
						.map(item -> new InvestigationEnforcementSummaryResponse(
								item.enforcementId(),
								item.actionType().name(),
								item.reasonCode(),
								item.status(),
								item.expiresAt(),
								item.possiblyExpired()
						))
						.toList()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewUserProfileForInvestigationUseCase.successMessage(),
				data
		));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
