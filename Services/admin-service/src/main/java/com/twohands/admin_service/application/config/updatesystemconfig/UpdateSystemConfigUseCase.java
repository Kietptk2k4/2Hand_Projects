package com.twohands.admin_service.application.config.updatesystemconfig;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.config.SystemConfigOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigHistory;
import com.twohands.admin_service.domain.config.SystemConfigHistoryRepository;
import com.twohands.admin_service.domain.config.SystemConfigPolicy;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UpdateSystemConfigUseCase {

	private static final Logger log = LoggerFactory.getLogger(UpdateSystemConfigUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_CONFIG_UPDATE";
	private static final String OUTBOX_EVENT_TYPE = "SYSTEM_CONFIG_UPDATED";
	private static final String OUTBOX_CHANGE_TYPE = "UPDATED";
	private static final String SUCCESS_MESSAGE = "System config updated successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigRepository systemConfigRepository;
	private final SystemConfigHistoryRepository systemConfigHistoryRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final SystemConfigOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public UpdateSystemConfigUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemConfigRepository systemConfigRepository,
			SystemConfigHistoryRepository systemConfigHistoryRepository,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			SystemConfigOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemConfigRepository = systemConfigRepository;
		this.systemConfigHistoryRepository = systemConfigHistoryRepository;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public UpdateSystemConfigResult execute(UpdateSystemConfigCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_UPDATE);

		SystemConfig existing = systemConfigRepository.findById(command.configId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		String reason = command.reason().trim();
		String newValue = SystemConfigPolicy.normalizeConfigValue(command.configValue(), existing.valueType());
		String newDescription = resolveDescription(command.description(), existing.description());

		SystemConfigPolicy.validateUpdateRequest(newValue, existing.valueType(), command.description(), reason);

		String oldValue = existing.configValue();
		Instant now = Instant.now();
		log.info(
				"Updating system config. adminId={}, configId={}, configKey={}",
				adminId,
				existing.id(),
				existing.configKey()
		);

		try {
			SystemConfig updated = systemConfigRepository.save(new SystemConfig(
					existing.id(),
					existing.configKey(),
					newValue,
					existing.valueType(),
					newDescription,
					existing.active(),
					existing.createdBy(),
					existing.createdAt(),
					adminId,
					now
			));

			SystemConfigHistory history = systemConfigHistoryRepository.save(new SystemConfigHistory(
					UUID.randomUUID(),
					updated.configKey(),
					oldValue,
					updated.configValue(),
					adminId,
					reason,
					now
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					updated.id(),
					outboxPayloadBuilder.buildSystemConfigUpdatedPayload(updated, OUTBOX_CHANGE_TYPE)
			));

			Map<String, Object> beforeSummary = new LinkedHashMap<>();
			beforeSummary.put("config_value", oldValue);
			if (existing.description() != null) {
				beforeSummary.put("description", existing.description());
			}

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("config_value", updated.configValue());
			if (updated.description() != null) {
				afterSummary.put("description", updated.description());
			}
			afterSummary.put("value_type", updated.valueType().name());

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					updated.configKey(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"System config value updated",
					beforeSummary,
					afterSummary,
					Map.of("reason", reason),
					Map.of(
							"config_id", updated.id().toString(),
							"history_id", history.id().toString(),
							"outbox_event_id", outboxEvent.id().toString()
					)
			);

			return new UpdateSystemConfigResult(
					updated.id(),
					updated.configKey(),
					updated.configValue(),
					updated.valueType(),
					updated.description(),
					updated.active(),
					updated.updatedBy(),
					updated.updatedAt(),
					history.id(),
					outboxEvent.id()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					existing.configKey(),
					ex.getMessage(),
					Map.of(
							"config_id", command.configId().toString(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private String resolveDescription(String requestedDescription, String currentDescription) {
		if (requestedDescription == null) {
			return currentDescription;
		}
		String trimmed = requestedDescription.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
