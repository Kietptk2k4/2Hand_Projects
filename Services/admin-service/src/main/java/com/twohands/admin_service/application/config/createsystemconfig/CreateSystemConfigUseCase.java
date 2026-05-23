package com.twohands.admin_service.application.config.createsystemconfig;

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
import com.twohands.admin_service.domain.config.SystemConfigValueType;
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
public class CreateSystemConfigUseCase {

	private static final Logger log = LoggerFactory.getLogger(CreateSystemConfigUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_CONFIG_CREATE";
	private static final String OUTBOX_EVENT_TYPE = "SYSTEM_CONFIG_UPDATED";
	private static final String OUTBOX_CHANGE_TYPE = "CREATED";
	private static final String SUCCESS_MESSAGE = "System config created successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigRepository systemConfigRepository;
	private final SystemConfigHistoryRepository systemConfigHistoryRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final SystemConfigOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public CreateSystemConfigUseCase(
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
	public CreateSystemConfigResult execute(CreateSystemConfigCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_UPDATE);

		SystemConfigValueType valueType = SystemConfigPolicy.parseValueType(command.valueType());
		String configKey = command.configKey().trim();
		String configValue = normalizeConfigValue(command.configValue(), valueType);
		String description = command.description() == null ? null : command.description().trim();
		boolean active = command.active() == null || command.active();
		String reason = command.reason().trim();

		SystemConfigPolicy.validateCreateRequest(configKey, configValue, valueType, description, reason);

		if (systemConfigRepository.existsByConfigKey(configKey)) {
			throw new AppException(ErrorCode.SYSTEM_CONFIG_CONFLICT, ErrorCode.SYSTEM_CONFIG_CONFLICT.defaultMessage());
		}

		Instant now = Instant.now();
		UUID configId = UUID.randomUUID();
		log.info("Creating system config. adminId={}, configKey={}, configId={}", adminId, configKey, configId);

		try {
			SystemConfig config = systemConfigRepository.save(new SystemConfig(
					configId,
					configKey,
					configValue,
					valueType,
					description,
					active,
					adminId,
					now,
					adminId,
					now
			));

			SystemConfigHistory history = systemConfigHistoryRepository.save(new SystemConfigHistory(
					UUID.randomUUID(),
					config.configKey(),
					null,
					config.configValue(),
					adminId,
					reason,
					now
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					config.id(),
					outboxPayloadBuilder.buildSystemConfigUpdatedPayload(config, OUTBOX_CHANGE_TYPE)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("config_key", config.configKey());
			afterSummary.put("value_type", config.valueType().name());
			afterSummary.put("is_active", config.active());

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					config.configKey(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"System config created",
					Map.of(),
					afterSummary,
					Map.of("reason", reason),
					Map.of(
							"config_id", config.id().toString(),
							"history_id", history.id().toString(),
							"outbox_event_id", outboxEvent.id().toString()
					)
			);

			return new CreateSystemConfigResult(
					config.id(),
					config.configKey(),
					config.configValue(),
					config.valueType(),
					config.description(),
					config.active(),
					config.createdBy(),
					config.createdAt(),
					history.id(),
					outboxEvent.id()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					configKey,
					ex.getMessage(),
					Map.of(
							"config_key", configKey,
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private String normalizeConfigValue(String configValue, SystemConfigValueType valueType) {
		String trimmed = configValue.trim();
		if (valueType == SystemConfigValueType.BOOLEAN) {
			return trimmed.toLowerCase();
		}
		return trimmed;
	}
}
