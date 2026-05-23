package com.twohands.admin_service.integration.config;

import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigCommand;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UpdateSystemConfigIntegrationTest {

	@Autowired
	private CreateSystemConfigUseCase createSystemConfigUseCase;

	@Autowired
	private UpdateSystemConfigUseCase updateSystemConfigUseCase;

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
	void execute_updatesValueWritesHistoryOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				"AUTO_COMPLETE_ORDER_DAYS",
				"7",
				"INTEGER",
				"Auto complete after delivery",
				true,
				"Initial value"
		));

		var updated = updateSystemConfigUseCase.execute(new UpdateSystemConfigCommand(
				created.configId(),
				"14",
				"Extended auto-complete window",
				"Ops requested longer window"
		));

		var config = systemConfigJpaRepository.findById(created.configId()).orElseThrow();
		assertEquals("14", config.getConfigValue());
		assertEquals("Extended auto-complete window", config.getDescription());
		assertEquals(adminId, config.getUpdatedBy());

		var history = systemConfigHistoryJpaRepository.findById(updated.historyId()).orElseThrow();
		assertEquals("7", history.getOldValue());
		assertEquals("14", history.getNewValue());
		assertEquals("Ops requested longer window", history.getReason());

		var outbox = outboxEventJpaRepository.findById(updated.outboxEventId()).orElseThrow();
		assertEquals("SYSTEM_CONFIG_UPDATED", outbox.getEventType());
		assertNotNull(outbox.getPayload());

		long updateAuditCount = adminActionLogJpaRepository.findAll().stream()
				.filter(log -> "SYSTEM_CONFIG_UPDATE".equals(log.getActionType().name()))
				.count();
		assertEquals(1, updateAuditCount);
	}
}
