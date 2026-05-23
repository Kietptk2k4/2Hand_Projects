package com.twohands.admin_service.application.audit;

import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionCommand;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionResult;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.audit.AdminRequestContextProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Facade for other use cases to append admin audit logs (FR_LogAdminAction).
 */
@Component
public class AdminActionAuditLogger {

	public static final String ACTION_ADMIN_ACCESS_DENIED = "ADMIN_ACCESS_DENIED";

	private final LogAdminActionUseCase logAdminActionUseCase;
	private final AdminRequestContextProvider requestContextProvider;

	public AdminActionAuditLogger(
			LogAdminActionUseCase logAdminActionUseCase,
			AdminRequestContextProvider requestContextProvider
	) {
		this.logAdminActionUseCase = logAdminActionUseCase;
		this.requestContextProvider = requestContextProvider;
	}

	public LogAdminActionResult logSuccess(
			UUID adminId,
			String actionType,
			String targetType,
			String targetId,
			String message,
			Map<String, Object> requestData,
			Map<String, Object> responseData
	) {
		return logAdminActionUseCase.execute(buildCommand(
				adminId,
				actionType,
				targetType,
				targetId,
				AdminActionStatus.SUCCESS,
				message,
				requestData,
				responseData,
				false
		));
	}

	public LogAdminActionResult logFailure(
			UUID adminId,
			String actionType,
			String targetType,
			String targetId,
			String message,
			Map<String, Object> requestData
	) {
		return logAdminActionUseCase.execute(buildCommand(
				adminId,
				actionType,
				targetType,
				targetId,
				AdminActionStatus.FAILURE,
				message,
				requestData,
				Map.of(),
				false
		));
	}

	public LogAdminActionResult logAccessDenied(
			UUID adminId,
			String permissionOrRole,
			String endpoint,
			String reason
	) {
		return logAdminActionUseCase.executeInNewTransaction(buildCommand(
				adminId,
				ACTION_ADMIN_ACCESS_DENIED,
				AdminActionTargetType.SECURITY,
				permissionOrRole != null ? permissionOrRole : "UNKNOWN",
				AdminActionStatus.FAILURE,
				reason,
				Map.of(
						"endpoint", endpoint != null ? endpoint : "",
						"required", permissionOrRole != null ? permissionOrRole : ""
				),
				Map.of(),
				false
		));
	}

	private LogAdminActionCommand buildCommand(
			UUID adminId,
			String actionType,
			String targetType,
			String targetId,
			AdminActionStatus status,
			String message,
			Map<String, Object> requestData,
			Map<String, Object> responseData,
			boolean storePayloadOverride
	) {
		return new LogAdminActionCommand(
				adminId,
				actionType,
				targetType,
				targetId,
				status,
				message,
				requestData,
				responseData,
				requestContextProvider.clientIpAddress(),
				requestContextProvider.userAgent(),
				storePayloadOverride
		);
	}
}
