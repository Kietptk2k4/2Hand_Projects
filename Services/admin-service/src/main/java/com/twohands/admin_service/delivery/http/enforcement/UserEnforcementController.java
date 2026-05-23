package com.twohands.admin_service.delivery.http.enforcement;

import com.twohands.admin_service.application.enforcement.banuser.BanUserCommand;
import com.twohands.admin_service.application.enforcement.banuser.BanUserResult;
import com.twohands.admin_service.application.enforcement.banuser.BanUserUseCase;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserCommand;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserResult;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserUseCase;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryQuery;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryResult;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryUseCase;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementQuery;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementResult;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/users")
public class UserEnforcementController {

	private final SuspendUserUseCase suspendUserUseCase;
	private final BanUserUseCase banUserUseCase;
	private final RestrictUserUseCase restrictUserUseCase;
	private final ViewCurrentUserEnforcementUseCase viewCurrentUserEnforcementUseCase;
	private final ViewUserEnforcementHistoryUseCase viewUserEnforcementHistoryUseCase;

	public UserEnforcementController(
			SuspendUserUseCase suspendUserUseCase,
			BanUserUseCase banUserUseCase,
			RestrictUserUseCase restrictUserUseCase,
			ViewCurrentUserEnforcementUseCase viewCurrentUserEnforcementUseCase,
			ViewUserEnforcementHistoryUseCase viewUserEnforcementHistoryUseCase
	) {
		this.suspendUserUseCase = suspendUserUseCase;
		this.banUserUseCase = banUserUseCase;
		this.restrictUserUseCase = restrictUserUseCase;
		this.viewCurrentUserEnforcementUseCase = viewCurrentUserEnforcementUseCase;
		this.viewUserEnforcementHistoryUseCase = viewUserEnforcementHistoryUseCase;
	}

	@GetMapping("/{userId}/enforcements/current")
	@RequireAdminPermission(AdminPermission.USER_ENFORCEMENT_READ)
	public ResponseEntity<ApiResponse<ViewCurrentUserEnforcementResponse>> viewCurrent(
			@PathVariable UUID userId
	) {
		ViewCurrentUserEnforcementResult result = viewCurrentUserEnforcementUseCase.execute(
				new ViewCurrentUserEnforcementQuery(userId)
		);

		ViewCurrentUserEnforcementResponse data = new ViewCurrentUserEnforcementResponse(
				result.userId(),
				result.enforcements().stream()
						.map(item -> new CurrentUserEnforcementResponse(
								item.enforcementId(),
								item.userId(),
								item.actionType().name(),
								item.reasonCode(),
								item.description(),
								item.expiresAt(),
								item.enforcedBy(),
								item.createdAt(),
								item.possiblyExpired()
						))
						.toList()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewCurrentUserEnforcementUseCase.successMessage(),
				data
		));
	}

	@GetMapping("/{userId}/enforcements/history")
	@RequireAdminPermission(AdminPermission.USER_ENFORCEMENT_READ)
	public ResponseEntity<ApiResponse<ViewUserEnforcementHistoryResponse>> viewHistory(
			@PathVariable UUID userId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ViewUserEnforcementHistoryResult result = viewUserEnforcementHistoryUseCase.execute(
				new ViewUserEnforcementHistoryQuery(userId, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewUserEnforcementHistoryUseCase.successMessage(),
				toHistoryResponse(result)
		));
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

	@PostMapping("/{userId}/ban")
	@RequireAdminPermission({AdminPermission.USER_BAN, AdminPermission.USER_SUSPEND})
	public ResponseEntity<ApiResponse<SuspendUserResponse>> ban(
			@PathVariable UUID userId,
			@Valid @RequestBody SuspendUserRequest request,
			HttpServletRequest httpServletRequest
	) {
		BanUserResult result = banUserUseCase.execute(new BanUserCommand(
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
				.body(ApiResponse.success(HttpStatus.OK.value(), banUserUseCase.successMessage(), data));
	}

	@PostMapping("/{userId}/restrict")
	@RequireAdminPermission(AdminPermission.USER_RESTRICT)
	public ResponseEntity<ApiResponse<SuspendUserResponse>> restrict(
			@PathVariable UUID userId,
			@Valid @RequestBody SuspendUserRequest request,
			HttpServletRequest httpServletRequest
	) {
		RestrictUserResult result = restrictUserUseCase.execute(new RestrictUserCommand(
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
				.body(ApiResponse.success(HttpStatus.OK.value(), restrictUserUseCase.successMessage(), data));
	}

	private ViewUserEnforcementHistoryResponse toHistoryResponse(ViewUserEnforcementHistoryResult result) {
		return new ViewUserEnforcementHistoryResponse(
				result.userId(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.enforcements().stream()
						.map(item -> new UserEnforcementHistoryItemResponse(
								item.enforcementId(),
								item.userId(),
								item.actionType().name(),
								item.reasonCode(),
								item.description(),
								item.expiresAt(),
								item.enforcedBy(),
								item.status().name(),
								item.createdAt(),
								item.updatedAt(),
								item.logs().stream()
										.map(log -> new UserEnforcementTransitionLogResponse(
												log.logId(),
												log.oldStatus(),
												log.newStatus(),
												log.adminId(),
												log.actorType(),
												log.note(),
												log.createdAt()
										))
										.toList()
						))
						.toList()
		);
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
