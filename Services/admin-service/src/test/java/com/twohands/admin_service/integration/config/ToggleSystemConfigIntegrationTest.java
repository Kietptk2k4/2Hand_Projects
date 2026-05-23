package com.twohands.admin_service.integration.config;

import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
import com.twohands.admin_service.application.config.togglesystemconfig.ToggleSystemConfigCommand;
import com.twohands.admin_service.application.config.togglesystemconfig.ToggleSystemConfigUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemConfigHistoryJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemConfigJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ToggleSystemConfigIntegrationTest {

	@Autowired
	private CreateSystemConfigUseCase createSystemConfigUseCase;

	@Autowired
	private ToggleSystemConfigUseCase toggleSystemConfigUseCase;

	@Autowired
	private SystemConfigJpaRepository systemConfigJpaRepository;

	@Autowired
	private SystemConfigHistoryJpaRepository systemConfigHistoryJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_deactivatesConfigAndWritesHistoryOutboxAudit() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				"ALLOW_NEW_SELLER",
				"true",
				"BOOLEAN",
				"Allow new seller registration",
				true,
				"Initial enable"
		));

		var toggled = toggleSystemConfigUseCase.execute(new ToggleSystemConfigCommand(
				created.configId(),
				false,
				"Temporarily disable signups"
		));

		assertFalse(toggled.active());
		assertEquals(true, toggled.stateChanged());

		var config = systemConfigJpaRepository.findById(created.configId()).orElseThrow();
		assertFalse(config.isActive());

		var history = systemConfigHistoryJpaRepository.findById(toggled.historyId()).orElseThrow();
		assertEquals("true", history.getOldValue());
		assertEquals("false", history.getNewValue());
		assertEquals("Temporarily disable signups", history.getReason());

		var outbox = outboxEventJpaRepository.findById(toggled.outboxEventId()).orElseThrow();
		assertEquals("SYSTEM_CONFIG_UPDATED", outbox.getEventType());
		assertNotNull(outbox.getPayload());

		long toggleAuditCount = adminActionLogJpaRepository.findAll().stream()
				.filter(log -> "SYSTEM_CONFIG_TOGGLE".equals(log.getActionType().name()))
				.count();
		assertEquals(1, toggleAuditCount);
	}

	@Test
	void execute_isIdempotentWhenAlreadyInTargetState() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				"MAX_IMAGES_PER_PRODUCT",
				"10",
				"INTEGER",
				null,
				true,
				"Initial"
		));

		var toggled = toggleSystemConfigUseCase.execute(new ToggleSystemConfigCommand(
				created.configId(),
				true,
				"Already active"
		));

		assertEquals(false, toggled.stateChanged());
		assertNull(toggled.historyId());
		assertNull(toggled.outboxEventId());
	}
}
