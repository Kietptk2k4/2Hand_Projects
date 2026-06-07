package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.admin.searchusersforinvestigation.SearchUsersForInvestigationCommand;
import com.twohands.auth_service.application.admin.searchusersforinvestigation.SearchUsersForInvestigationResult;
import com.twohands.auth_service.application.admin.searchusersforinvestigation.SearchUsersForInvestigationUseCase;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminCommand;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminResult;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminUseCase;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminCommand;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminQueryValidationService;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminResult;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminUseCase;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminCommand;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminQueryValidationService;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminResult;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.response.SearchUsersForInvestigationResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewLoginHistoryForAdminResponse;
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

	private final SearchUsersForInvestigationUseCase searchUsersForInvestigationUseCase;
	private final ViewUserInvestigationProfileByAdminUseCase viewUserInvestigationProfileByAdminUseCase;
	private final ViewUserSessionsForAdminUseCase viewUserSessionsForAdminUseCase;
	private final ViewLoginHistoryForAdminUseCase viewLoginHistoryForAdminUseCase;

	public AdminUserInvestigationController(
			SearchUsersForInvestigationUseCase searchUsersForInvestigationUseCase,
			ViewUserInvestigationProfileByAdminUseCase viewUserInvestigationProfileByAdminUseCase,
			ViewUserSessionsForAdminUseCase viewUserSessionsForAdminUseCase,
			ViewLoginHistoryForAdminUseCase viewLoginHistoryForAdminUseCase
	) {
		this.searchUsersForInvestigationUseCase = searchUsersForInvestigationUseCase;
		this.viewUserInvestigationProfileByAdminUseCase = viewUserInvestigationProfileByAdminUseCase;
		this.viewUserSessionsForAdminUseCase = viewUserSessionsForAdminUseCase;
		this.viewLoginHistoryForAdminUseCase = viewLoginHistoryForAdminUseCase;
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<SearchUsersForInvestigationResponse>> searchUsers(
			@RequestParam(name = "query", defaultValue = "") String query,
			@RequestParam(defaultValue = "20") int limit
	) {
		SearchUsersForInvestigationResult result = searchUsersForInvestigationUseCase.execute(
				new SearchUsersForInvestigationCommand(resolveAuthenticatedUserId(), query, limit)
		);

		SearchUsersForInvestigationResponse response = new SearchUsersForInvestigationResponse(
				result.users().stream()
						.map(user -> new SearchUsersForInvestigationResponse.UserItem(
								user.userId().toString(),
								user.email(),
								user.displayName(),
								user.status(),
								user.roleCodes()
						))
						.toList()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(
						HttpStatus.OK.value(),
						searchUsersForInvestigationUseCase.successMessage(),
						response
				));
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

	@GetMapping("/{userId}/login-history")
	public ResponseEntity<ApiResponse<ViewLoginHistoryForAdminResponse>> getLoginHistory(
			@PathVariable String userId,
			@RequestParam(defaultValue = "" + ViewLoginHistoryForAdminQueryValidationService.DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = "" + ViewLoginHistoryForAdminQueryValidationService.DEFAULT_LIMIT) int limit,
			@RequestParam(required = false) Boolean success,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to
	) {
		UUID targetUserId = parseUserId(userId);

		ViewLoginHistoryForAdminResult result = viewLoginHistoryForAdminUseCase.execute(
				new ViewLoginHistoryForAdminCommand(
						resolveAuthenticatedUserId(),
						targetUserId,
						page,
						limit,
						success,
						from,
						to
				)
		);

		ViewLoginHistoryForAdminResponse response = new ViewLoginHistoryForAdminResponse(
				result.userId().toString(),
				result.items().stream()
						.map(item -> new ViewLoginHistoryForAdminResponse.ItemData(
								item.loginMethod(),
								item.ipAddress(),
								item.userAgent(),
								item.success(),
								item.createdAt()
						))
						.toList(),
				new ViewLoginHistoryForAdminResponse.PaginationData(
						result.pagination().page(),
						result.pagination().limit(),
						result.pagination().totalItems(),
						result.pagination().totalPages(),
						result.pagination().hasNext()
				)
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(
						HttpStatus.OK.value(),
						viewLoginHistoryForAdminUseCase.successMessage(),
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
