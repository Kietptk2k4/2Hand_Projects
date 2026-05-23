package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.adminlogout.AdminLogoutCommand;
import com.twohands.admin_service.application.auth.adminlogout.AdminLogoutUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.auth.AdminLogoutDelegation;
import com.twohands.admin_service.domain.auth.AuthLogoutGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLogoutUseCaseTest {

	private final AuthLogoutGateway authLogoutGateway = mock(AuthLogoutGateway.class);
	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final AdminLogoutUseCase useCase = new AdminLogoutUseCase(authLogoutGateway, adminAuthorizationService);

	@Test
	void shouldDelegateLogoutWhenGatewayEnabled() {
		UUID adminId = UUID.randomUUID();
		when(authLogoutGateway.isEnabled()).thenReturn(true);
		when(adminAuthorizationService.requireCurrentAdmin())
				.thenReturn(new AuthenticatedUser(adminId, List.of("ADMIN"), List.of("ADMIN_ACCESS")));
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		useCase.execute(new AdminLogoutCommand("refresh-token", "127.0.0.1", "jwt-access"));

		verify(authLogoutGateway).logout(any(AdminLogoutDelegation.class));
		verify(adminAuthorizationService).requireCurrentAdmin();
	}

	@Test
	void shouldRejectWhenGatewayDisabled() {
		when(authLogoutGateway.isEnabled()).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new AdminLogoutCommand("refresh-token", null, "jwt")))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
	}
}
