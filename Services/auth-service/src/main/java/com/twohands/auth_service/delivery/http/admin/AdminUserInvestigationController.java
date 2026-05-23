package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminCommand;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminResult;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminUseCase;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminCommand;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminQueryValidationService;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminResult;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewUserInvestigationProfileByAdminResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewUserSessionsForAdminResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserInvestigationController {

	private final ViewUserInvestigationProfileByAdminUseCase viewUserInvestigationProfileByAdminUseCase;
	private final ViewUserSessionsForAdminUseCase viewUserSessionsForAdminUseCase;

	public AdminUserInvestigationController(
			ViewUserInvestigationProfileByAdminUseCase viewUserInvestigationProfileByAdminUseCase,
			ViewUserSessionsForAdminUseCase viewUserSessionsForAdminUseCase
	) {
		this.viewUserInvestigationProfileByAdminUseCase = viewUserInvestigationProfileByAdminUseCase;
		this.viewUserSessionsForAdminUseCase = viewUserSessionsForAdminUseCase;
	}

	@GetMapping("/{userId}/investigation-profile")
	public ResponseEntity<ApiResponse<ViewUserInvestigationProfileByAdminResponse>> getInvestigationProfile(
			@PathVariable String userId
	) {
		UUID targetUserId = parseUserId(userId);

		ViewUserInvestigationProfileByAdminResult result = viewUserInvestigationProfileByAdminUseCase.execute(
				new ViewUserInvestigationProfileByAdminCommand(resolveAuthenticatedUserId(), targetUserId)
		);

		ViewUserInvestigationProfileByAdminResponse response = new ViewUserInvestigationProfileByAdminResponse(
				result.userId().toString(),
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
				result.isPrivate()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(
						HttpStatus.OK.value(),
						viewUserInvestigationProfileByAdminUseCase.successMessage(),
						response
				));
	}

	@GetMapping("/{userId}/sessions")
	public ResponseEntity<ApiResponse<ViewUserSessionsForAdminResponse>> getUserSessions(
			@PathVariable String userId,
			@RequestParam(defaultValue = "ACTIVE") String status,
			@RequestParam(defaultValue = "" + ViewUserSessionsForAdminQueryValidationService.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = "" + ViewUserSessionsForAdminQueryValidationService.DEFAULT_LIMIT) int limit
	) {
		UUID targetUserId = parseUserId(userId);

		ViewUserSessionsForAdminResult result = viewUserSessionsForAdminUseCase.execute(
				new ViewUserSessionsForAdminCommand(resolveAuthenticatedUserId(), targetUserId, status, page, limit)
		);

		ViewUserSessionsForAdminResponse response = new ViewUserSessionsForAdminResponse(
				result.userId().toString(),
				result.sessions().stream()
						.map(session -> new ViewUserSessionsForAdminResponse.SessionData(
								session.sessionId().toString(),
								session.deviceId(),
								session.ipAddress(),
								session.userAgent(),
								session.status(),
								session.createdAt(),
								session.updatedAt()
						))
						.toList(),
				new ViewUserSessionsForAdminResponse.PaginationData(
						result.pagination().page(),
						result.pagination().limit(),
						result.pagination().totalItems(),
						result.pagination().hasNext()
				)
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(
						HttpStatus.OK.value(),
						viewUserSessionsForAdminUseCase.successMessage(),
						response
				));
	}

	private UUID parseUserId(String rawUserId) {
		try {
			return UUID.fromString(rawUserId);
		} catch (IllegalArgumentException ex) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "Du lieu khong hop le.", "userId", "INVALID_FORMAT");
		}
	}

	private UUID resolveAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}
		return UUID.fromString(authentication.getName());
	}
}
