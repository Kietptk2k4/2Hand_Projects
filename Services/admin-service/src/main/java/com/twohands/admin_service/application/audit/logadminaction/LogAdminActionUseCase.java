package com.twohands.admin_service.application.audit.logadminaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import com.twohands.admin_service.domain.audit.AdminActionLogPolicy;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AuditPayloadSanitizer;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class LogAdminActionUseCase {

	private final AdminActionLogRepository adminActionLogRepository;
	private final AuditPayloadSanitizer auditPayloadSanitizer;
	private final ObjectMapper objectMapper;

	public LogAdminActionUseCase(
			AdminActionLogRepository adminActionLogRepository,
			AuditPayloadSanitizer auditPayloadSanitizer,
			ObjectMapper objectMapper
	) {
		this.adminActionLogRepository = adminActionLogRepository;
		this.auditPayloadSanitizer = auditPayloadSanitizer;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public LogAdminActionResult execute(LogAdminActionCommand command) {
		validate(command);
		AdminActionLog saved = adminActionLogRepository.save(toDomain(command));
		return new LogAdminActionResult(
				saved.id(),
				saved.adminId(),
				saved.actionType(),
				saved.targetType(),
				saved.targetId(),
				saved.createdAt()
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public LogAdminActionResult executeInNewTransaction(LogAdminActionCommand command) {
		return execute(command);
	}

	private AdminActionLog toDomain(LogAdminActionCommand command) {
		boolean storePayload = command.storePayloadOverride()
				|| AdminActionLogPolicy.isCriticalAction(command.actionType());
		String requestPayload = storePayload ? auditPayloadSanitizer.sanitizeToJson(command.requestData()) : null;
		String responsePayload = buildResponsePayload(command, storePayload);
		return new AdminActionLog(
				UUID.randomUUID(),
				command.adminId(),
				command.actionType().trim().toUpperCase(),
				command.targetType().trim().toUpperCase(),
				command.targetId().trim(),
				command.status(),
				requestPayload,
				responsePayload,
				command.ipAddress(),
				command.userAgent(),
				Instant.now()
		);
	}

	private String buildResponsePayload(LogAdminActionCommand command, boolean storePayload) {
		ObjectNode envelope = objectMapper.createObjectNode();
		envelope.put("status", command.status().name());
		if (command.message() != null && !command.message().isBlank()) {
			envelope.put("message", command.message());
		}
		if (storePayload && command.responseData() != null && !command.responseData().isEmpty()) {
			String sanitized = auditPayloadSanitizer.sanitizeToJson(command.responseData());
			if (sanitized != null) {
				try {
					envelope.set("result", objectMapper.readTree(sanitized));
				} catch (JsonProcessingException ex) {
					throw new AppException(ErrorCode.BAD_REQUEST, "Failed to build audit response payload");
				}
			}
		}
		return envelope.toString();
	}

	private void validate(LogAdminActionCommand command) {
		if (command.adminId() == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "adminId is required", "adminId", "must not be null");
		}
		if (command.actionType() == null || command.actionType().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "actionType is required", "actionType", "must not be blank");
		}
		if (command.targetType() == null || command.targetType().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "targetType is required", "targetType", "must not be blank");
		}
		if (command.status() == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "status is required", "status", "must not be null");
		}
	}
}
