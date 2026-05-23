package com.twohands.admin_service.integration.config;

import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateSystemConfigIntegrationTest {

	@Autowired
	private CreateSystemConfigUseCase createSystemConfigUseCase;

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
	void execute_persistsConfigHistoryOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var result = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				"PAYMENT_EXPIRE_MINUTES",
				"30",
				"INTEGER",
				"Payment timeout",
				true,
				"Commerce payment SLA"
		));

		var config = systemConfigJpaRepository.findById(result.configId()).orElseThrow();
		assertEquals("PAYMENT_EXPIRE_MINUTES", config.getConfigKey());
		assertEquals("30", config.getConfigValue());
		assertEquals("INTEGER", config.getValueType().name());
		assertEquals(true, config.isActive());

		var history = systemConfigHistoryJpaRepository.findById(result.historyId()).orElseThrow();
		assertEquals("PAYMENT_EXPIRE_MINUTES", history.getConfigKey());
		assertNull(history.getOldValue());
		assertEquals("30", history.getNewValue());
		assertEquals("Commerce payment SLA", history.getReason());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("SYSTEM_CONFIG_UPDATED", outbox.getEventType());
		assertEquals(result.configId(), outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "SYSTEM_CONFIG_CREATE".equals(log.getActionType().name()))
				.count());
	}
}
