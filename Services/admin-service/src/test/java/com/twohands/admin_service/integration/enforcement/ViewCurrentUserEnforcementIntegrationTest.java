package com.twohands.admin_service.integration.enforcement;

import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserCommand;
import com.twohands.admin_service.application.enforcement.restrictuser.RestrictUserUseCase;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementCommand;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementUseCase;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementQuery;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementUseCase;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewCurrentUserEnforcementIntegrationTest {

	@Autowired
	private RestrictUserUseCase restrictUserUseCase;

	@Autowired
	private RevokeUserEnforcementUseCase revokeUserEnforcementUseCase;

	@Autowired
	private ViewCurrentUserEnforcementUseCase viewCurrentUserEnforcementUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private AuthUserEnforcementGateway authUserEnforcementGateway;

	@MockBean
	private AuthUserLookupGateway authUserLookupGateway;

	@Test
	void execute_returnsOnlyActiveEnforcements() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);
		when(authUserLookupGateway.isEnabled()).thenReturn(false);

		var created = restrictUserUseCase.execute(new RestrictUserCommand(
				userId,
				"SPAM",
				"Active restrict",
				null,
				""
		));

		var current = viewCurrentUserEnforcementUseCase.execute(new ViewCurrentUserEnforcementQuery(userId));
		assertEquals(1, current.enforcements().size());
		assertEquals("RESTRICT", current.enforcements().get(0).actionType().name());

		revokeUserEnforcementUseCase.execute(new RevokeUserEnforcementCommand(
				created.enforcementId(),
				"revoked",
				null,
				""
		));

		var afterRevoke = viewCurrentUserEnforcementUseCase.execute(new ViewCurrentUserEnforcementQuery(userId));
		assertTrue(afterRevoke.enforcements().isEmpty());
	}
}
