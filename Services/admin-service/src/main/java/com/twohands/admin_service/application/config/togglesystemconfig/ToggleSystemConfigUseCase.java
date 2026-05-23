package com.twohands.admin_service.application.config.togglesystemconfig;

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
public class ToggleSystemConfigUseCase {

	private static final Logger log = LoggerFactory.getLogger(ToggleSystemConfigUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_CONFIG_TOGGLE";
	private static final String OUTBOX_EVENT_TYPE = "SYSTEM_CONFIG_UPDATED";
	private static final String OUTBOX_CHANGE_TYPE = "TOGGLED";
	private static final String SUCCESS_MESSAGE = "System config toggled successfully";
	private static final String IDEMPOTENT_MESSAGE = "System config is already in the requested active state";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigRepository systemConfigRepository;
	private final SystemConfigHistoryRepository systemConfigHistoryRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final SystemConfigOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ToggleSystemConfigUseCase(
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
	public ToggleSystemConfigResult execute(ToggleSystemConfigCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_UPDATE);

		String reason = command.reason().trim();
		SystemConfigPolicy.validateToggleRequest(reason);

		SystemConfig existing = systemConfigRepository.findById(command.configId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		if (existing.active() == command.active()) {
			log.info(
					"System config toggle idempotent. configId={}, configKey={}, isActive={}",
					existing.id(),
					existing.configKey(),
					existing.active()
			);
			return toResult(existing, null, null, false);
		}

		Instant now = Instant.now();
		boolean oldActive = existing.active();
		log.info(
				"Toggling system config. adminId={}, configId={}, configKey={}, isActive={} -> {}",
				adminId,
				existing.id(),
				existing.configKey(),
				oldActive,
				command.active()
		);

		try {
			SystemConfig updated = systemConfigRepository.save(new SystemConfig(
					existing.id(),
					existing.configKey(),
					existing.configValue(),
					existing.valueType(),
					existing.description(),
					command.active(),
					existing.createdBy(),
					existing.createdAt(),
					adminId,
					now
			));

			SystemConfigHistory history = systemConfigHistoryRepository.save(new SystemConfigHistory(
					UUID.randomUUID(),
					updated.configKey(),
					SystemConfigPolicy.formatActiveState(oldActive),
					SystemConfigPolicy.formatActiveState(updated.active()),
					adminId,
					reason,
					now
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					updated.id(),
					outboxPayloadBuilder.buildSystemConfigUpdatedPayload(updated, OUTBOX_CHANGE_TYPE)
			));

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					updated.configKey(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"System config active state toggled",
					Map.of("is_active", oldActive),
					Map.of("is_active", updated.active()),
					Map.of("reason", reason),
					Map.of(
							"config_id", updated.id().toString(),
							"history_id", history.id().toString(),
							"outbox_event_id", outboxEvent.id().toString()
					)
			);

			return toResult(updated, history.id(), outboxEvent.id(), true);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.CONFIG,
					existing.configKey(),
					ex.getMessage(),
					Map.of(
							"config_id", command.configId().toString(),
							"is_active", command.active(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage(boolean stateChanged) {
		return stateChanged ? SUCCESS_MESSAGE : IDEMPOTENT_MESSAGE;
	}

	private ToggleSystemConfigResult toResult(
			SystemConfig config,
			UUID historyId,
			UUID outboxEventId,
			boolean stateChanged
	) {
		return new ToggleSystemConfigResult(
				config.id(),
				config.configKey(),
				config.configValue(),
				config.valueType(),
				config.description(),
				config.active(),
				config.updatedBy(),
				config.updatedAt(),
				historyId,
				outboxEventId,
				stateChanged
		);
	}
}
