package com.twohands.admin_service.integration.enforcement;

import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserCommand;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserUseCase;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementCommand;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
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
class RevokeUserEnforcementIntegrationTest {

	@Autowired
	private RestrictUserUseCase restrictUserUseCase;

	@Autowired
	private RevokeUserEnforcementUseCase revokeUserEnforcementUseCase;

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
	void execute_revokesActiveEnforcementAndWritesLogOutboxAudit() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);

		var created = restrictUserUseCase.execute(new RestrictUserCommand(
				userId,
				"SPAM",
				"Restrict before revoke",
				null,
				""
		));

		var revoked = revokeUserEnforcementUseCase.execute(new RevokeUserEnforcementCommand(
				created.enforcementId(),
				"Revoked by integration test",
				null,
				""
		));

		var enforcement = userEnforcementJpaRepository.findById(revoked.enforcementId()).orElseThrow();
		assertEquals("REVOKED", enforcement.getStatus().name());
		assertEquals(UserEnforcementStatus.REVOKED.name(), revoked.status().name());

		long revokeLogs = userEnforcementLogJpaRepository.findAll().stream()
				.filter(l -> l.getEnforcement().getId().equals(created.enforcementId()))
				.filter(l -> l.getNewStatus() != null && "REVOKED".equals(l.getNewStatus().name()))
				.count();
		assertEquals(1, revokeLogs);

		var outbox = outboxEventJpaRepository.findById(revoked.outboxEventId()).orElseThrow();
		assertEquals("USER_ENFORCEMENT_REVOKED", outbox.getEventType());
		assertNotNull(outbox.getPayload());

		assertEquals(1, adminActionLogJpaRepository.findAll().stream()
				.filter(l -> "USER_ENFORCEMENT_REVOKE".equals(l.getActionType().name()))
				.count());
	}
}
