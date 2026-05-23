package com.twohands.admin_service.integration.enforcement;

import com.twohands.admin_service.application.enforcement.suspenduser.SuspendUserCommand;
import com.twohands.admin_service.application.enforcement.suspenduser.SuspendUserUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementLogJpaRepository;
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
class SuspendUserIntegrationTest {

	@Autowired
	private SuspendUserUseCase suspendUserUseCase;

	@Autowired
	private UserEnforcementJpaRepository userEnforcementJpaRepository;

	@Autowired
	private UserEnforcementLogJpaRepository userEnforcementLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private AuthUserEnforcementGateway authUserEnforcementGateway;

	@Test
	void execute_persistsEnforcementLogOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);

		var result = suspendUserUseCase.execute(new SuspendUserCommand(
				userId,
				"POLICY_VIOLATION",
				"Integration test suspend",
				null,
				""
		));

		var enforcement = userEnforcementJpaRepository.findById(result.enforcementId()).orElseThrow();
		assertEquals(userId, enforcement.getUserId());
		assertEquals("SUSPEND", enforcement.getActionType().name());
		assertEquals("ACTIVE", enforcement.getStatus().name());

		var logs = userEnforcementLogJpaRepository.findAll();
		assertEquals(1, logs.stream().filter(l -> l.getEnforcement().getId().equals(result.enforcementId())).count());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("USER_SUSPENDED", outbox.getEventType());
		assertEquals(userId, outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(l -> "USER_SUSPEND".equals(l.getActionType().name()))
				.count());
	}
}
