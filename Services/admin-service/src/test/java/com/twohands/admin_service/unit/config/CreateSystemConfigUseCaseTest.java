package com.twohands.admin_service.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.config.SystemConfigOutboxPayloadBuilder;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateSystemConfigUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemConfigRepository systemConfigRepository = mock(SystemConfigRepository.class);
	private final SystemConfigHistoryRepository systemConfigHistoryRepository = mock(SystemConfigHistoryRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private CreateSystemConfigUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CreateSystemConfigUseCase(
				adminAuthorizationService,
				systemConfigRepository,
				systemConfigHistoryRepository,
				insertAdminOutboxEventUseCase,
				new SystemConfigOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldCreateConfigWithHistoryAndOutbox() {
		UUID adminId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemConfigRepository.existsByConfigKey("MAX_CART_ITEMS")).thenReturn(false);
		when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(systemConfigHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(
						UUID.randomUUID(),
						"SYSTEM_CONFIG_UPDATED",
						UUID.randomUUID(),
						"{}",
						OutboxStatus.PENDING,
						0,
						now,
						null,
						null
				));

		var result = useCase.execute(new CreateSystemConfigCommand(
				"MAX_CART_ITEMS",
				"100",
				"INTEGER",
				"Max items per cart",
				true,
				"Initial platform limit"
		));

		assertThat(result.configKey()).isEqualTo("MAX_CART_ITEMS");
		assertThat(result.valueType()).isEqualTo(SystemConfigValueType.INTEGER);
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_CONFIG_UPDATE);
		verify(systemConfigHistoryRepository).save(any());
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldRejectDuplicateConfigKey() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.existsByConfigKey("MAX_CART_ITEMS")).thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(new CreateSystemConfigCommand(
				"MAX_CART_ITEMS",
				"100",
				"INTEGER",
				null,
				true,
				"duplicate"
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_CONFIG_CONFLICT);

		verify(systemConfigRepository, never()).save(any());
	}
}
