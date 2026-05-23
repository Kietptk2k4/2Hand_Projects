package com.twohands.admin_service.unit.session;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.session.revokeadminsession.RevokeAdminSessionCommand;
import com.twohands.admin_service.application.session.revokeadminsession.RevokeAdminSessionUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeRequest;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeResult;
import com.twohands.admin_service.domain.auth.AuthAdminSessionGateway;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevokeAdminSessionUseCaseTest {

	private final AuthAdminSessionGateway authAdminSessionGateway = mock(AuthAdminSessionGateway.class);
	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);
	private final RevokeAdminSessionUseCase useCase = new RevokeAdminSessionUseCase(
			authAdminSessionGateway,
			adminAuthorizationService,
			adminActionAuditLogger
	);

	@Test
	void shouldRevokeSessionAndAuditLog() {
		UUID actorId = UUID.randomUUID();
		UUID sessionId = UUID.randomUUID();
		UUID targetAdminId = UUID.randomUUID();
		when(authAdminSessionGateway.isEnabled()).thenReturn(true);
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(actorId);
		when(authAdminSessionGateway.revoke(any(AdminSessionRevokeRequest.class)))
				.thenReturn(new AdminSessionRevokeResult(targetAdminId, sessionId, 1, false));

		var result = useCase.execute(new RevokeAdminSessionCommand(sessionId, false, "jwt"));

		assertThat(result.revokedSessionCount()).isEqualTo(1);
		verify(adminAuthorizationService).requirePermission(AdminPermission.ADMIN_SESSION_REVOKE);
		verify(adminActionAuditLogger).logCritical(
				eq(actorId),
				eq("ADMIN_SESSION_REVOKE"),
				any(),
				eq(sessionId.toString()),
				any(),
				any(),
				any(),
				any(),
				any(),
				any(),
				any()
		);
	}

	@Test
	void shouldRejectWhenAuthIntegrationDisabled() {
		when(authAdminSessionGateway.isEnabled()).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new RevokeAdminSessionCommand(UUID.randomUUID(), false, "jwt")))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
	}

	@Test
	void shouldAuditFailureWhenAuthReturnsError() {
		UUID actorId = UUID.randomUUID();
		UUID sessionId = UUID.randomUUID();
		when(authAdminSessionGateway.isEnabled()).thenReturn(true);
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(actorId);
		doThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Session not found"))
				.when(authAdminSessionGateway).revoke(any(AdminSessionRevokeRequest.class));

		assertThatThrownBy(() -> useCase.execute(new RevokeAdminSessionCommand(sessionId, false, "jwt")))
				.isInstanceOf(AppException.class);

		verify(adminActionAuditLogger).logFailure(
				eq(actorId),
				eq("ADMIN_SESSION_REVOKE"),
				any(),
				eq(sessionId.toString()),
				any(),
				any()
		);
	}
}
