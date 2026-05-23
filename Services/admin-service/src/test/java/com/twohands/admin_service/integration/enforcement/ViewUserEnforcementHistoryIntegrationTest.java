package com.twohands.admin_service.integration.enforcement;

import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserCommand;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserUseCase;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementCommand;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementUseCase;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryQuery;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewUserEnforcementHistoryIntegrationTest {

	@Autowired
	private RestrictUserUseCase restrictUserUseCase;

	@Autowired
	private RevokeUserEnforcementUseCase revokeUserEnforcementUseCase;

	@Autowired
	private ViewUserEnforcementHistoryUseCase viewUserEnforcementHistoryUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private AuthUserEnforcementGateway authUserEnforcementGateway;

	@MockBean
	private AuthUserLookupGateway authUserLookupGateway;

	@Test
	void execute_returnsEnforcementWithTransitionLogs() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);
		when(authUserLookupGateway.isEnabled()).thenReturn(false);

		var created = restrictUserUseCase.execute(new RestrictUserCommand(
				userId,
				"SPAM",
				"History integration",
				null,
				""
		));

		revokeUserEnforcementUseCase.execute(new RevokeUserEnforcementCommand(
				created.enforcementId(),
				"revoked in test",
				null,
				""
		));

		var history = viewUserEnforcementHistoryUseCase.execute(new ViewUserEnforcementHistoryQuery(userId, 1, 10));

		assertEquals(1, history.totalElements());
		assertEquals(1, history.enforcements().size());
		assertEquals("REVOKED", history.enforcements().get(0).status().name());
		assertFalse(history.enforcements().get(0).logs().isEmpty());
		assertEquals(2, history.enforcements().get(0).logs().size());
	}
}
