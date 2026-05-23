package com.twohands.admin_service.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.config.SystemConfigOutboxPayloadBuilder;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigCommand;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigHistoryRepository;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateSystemConfigUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemConfigRepository systemConfigRepository = mock(SystemConfigRepository.class);
	private final SystemConfigHistoryRepository systemConfigHistoryRepository = mock(SystemConfigHistoryRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private UpdateSystemConfigUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new UpdateSystemConfigUseCase(
				adminAuthorizationService,
				systemConfigRepository,
				systemConfigHistoryRepository,
				insertAdminOutboxEventUseCase,
				new SystemConfigOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldUpdateConfigValueAndWriteHistory() {
		UUID adminId = UUID.randomUUID();
		UUID configId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemConfig existing = new SystemConfig(
				configId,
				"MAX_CART_ITEMS",
				"50",
				SystemConfigValueType.INTEGER,
				"Old description",
				true,
				adminId,
				now.minusSeconds(3600),
				adminId,
				now.minusSeconds(3600)
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(existing));
		when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(systemConfigHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(
						UUID.randomUUID(),
						"SYSTEM_CONFIG_UPDATED",
						configId,
						"{}",
						OutboxStatus.PENDING,
						0,
						now,
						null,
						null
				));

		var result = useCase.execute(new UpdateSystemConfigCommand(
				configId,
				"100",
				"Updated description",
				"Increase cart limit"
		));

		assertThat(result.configValue()).isEqualTo("100");
		assertThat(result.description()).isEqualTo("Updated description");
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_CONFIG_UPDATE);
		verify(systemConfigHistoryRepository).save(any());
	}

	@Test
	void shouldKeepDescriptionWhenNotProvided() {
		UUID adminId = UUID.randomUUID();
		UUID configId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemConfig existing = new SystemConfig(
				configId,
				"ALLOW_NEW_SELLER",
				"true",
				SystemConfigValueType.BOOLEAN,
				"Keep me",
				true,
				adminId,
				now,
				adminId,
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(existing));
		when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(systemConfigHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(UUID.randomUUID(), "SYSTEM_CONFIG_UPDATED", configId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new UpdateSystemConfigCommand(configId, "false", null, "Disable sellers"));

		assertThat(result.configValue()).isEqualTo("false");
		assertThat(result.description()).isEqualTo("Keep me");
	}

	@Test
	void shouldReturnNotFoundWhenConfigMissing() {
		UUID configId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new UpdateSystemConfigCommand(
				configId,
				"10",
				null,
				"reason"
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}
}
