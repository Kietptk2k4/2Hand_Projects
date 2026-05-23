package com.twohands.admin_service.application.audit.logcriticalpayload;

import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionCommand;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionResult;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.domain.audit.AdminActionLogPolicy;
import com.twohands.admin_service.domain.audit.AdminRequestContextProvider;
import com.twohands.admin_service.domain.audit.AuditPayloadSanitizer;
import com.twohands.admin_service.domain.audit.CriticalPayloadBuilder;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class LogCriticalAdminActionPayloadUseCase {

	private final LogAdminActionUseCase logAdminActionUseCase;
	private final CriticalPayloadBuilder criticalPayloadBuilder;
	private final AuditPayloadSanitizer auditPayloadSanitizer;
	private final AdminRequestContextProvider requestContextProvider;

	public LogCriticalAdminActionPayloadUseCase(
			LogAdminActionUseCase logAdminActionUseCase,
			CriticalPayloadBuilder criticalPayloadBuilder,
			AuditPayloadSanitizer auditPayloadSanitizer,
			AdminRequestContextProvider requestContextProvider
	) {
		this.logAdminActionUseCase = logAdminActionUseCase;
		this.criticalPayloadBuilder = criticalPayloadBuilder;
		this.auditPayloadSanitizer = auditPayloadSanitizer;
		this.requestContextProvider = requestContextProvider;
	}

	@Transactional
	public LogCriticalAdminActionPayloadResult execute(LogCriticalAdminActionPayloadCommand command) {
		validate(command);
		Map<String, Object> requestPayload = criticalPayloadBuilder.buildRequestPayload(
				command.summary(),
				command.before(),
				command.after(),
				command.additionalContext()
		);
		ensurePayloadPresent(requestPayload);

		Map<String, Object> responsePayload = criticalPayloadBuilder.buildResponsePayload(command.resultSummary());
		LogAdminActionResult saved = logAdminActionUseCase.execute(new LogAdminActionCommand(
				command.adminId(),
				command.actionType(),
				command.targetType(),
				command.targetId(),
				command.status(),
				command.message(),
				requestPayload,
				responsePayload,
				requestContextProvider.clientIpAddress(),
				requestContextProvider.userAgent(),
				true
		));

		String sanitizedRequest = auditPayloadSanitizer.sanitizeCriticalPayload(requestPayload);
		return new LogCriticalAdminActionPayloadResult(
				saved.logId(),
				saved.adminId(),
				saved.actionType(),
				sanitizedRequest,
				saved.createdAt()
		);
	}

	private void ensurePayloadPresent(Map<String, Object> requestPayload) {
		if (requestPayload == null || requestPayload.isEmpty()) {
			throw new AppException(
					ErrorCode.AUDIT_PAYLOAD_ERROR,
					"Critical audit payload must include summary, before, after, or context",
					"payload",
					"must not be empty for critical actions"
			);
		}
	}

	private void validate(LogCriticalAdminActionPayloadCommand command) {
		if (command.adminId() == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "adminId is required", "adminId", "must not be null");
		}
		if (command.actionType() == null || command.actionType().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "actionType is required", "actionType", "must not be blank");
		}
		if (!AdminActionLogPolicy.isCriticalAction(command.actionType().trim().toUpperCase())) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Payload logging applies only to critical admin actions",
					"actionType",
					"must be a critical action type"
			);
		}
		if (command.targetType() == null || command.targetType().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "targetType is required", "targetType", "must not be blank");
		}
		if (command.status() == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "status is required", "status", "must not be null");
		}
	}
}
