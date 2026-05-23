package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.adminlogin.AdminLoginCommand;
import com.twohands.admin_service.application.auth.adminlogin.AdminLoginUseCase;
import com.twohands.admin_service.domain.auth.AdminCredentialLogin;
import com.twohands.admin_service.domain.auth.AdminLoginTokens;
import com.twohands.admin_service.domain.auth.AuthLoginGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoginUseCaseTest {

	private final AuthLoginGateway authLoginGateway = mock(AuthLoginGateway.class);
	private final AdminLoginUseCase useCase = new AdminLoginUseCase(authLoginGateway);

	@Test
	void shouldDelegateLoginWhenGatewayEnabled() {
		UUID adminId = UUID.randomUUID();
		when(authLoginGateway.isEnabled()).thenReturn(true);
		when(authLoginGateway.login(any(AdminCredentialLogin.class))).thenReturn(new AdminLoginTokens(
				"access",
				"refresh",
				900L,
				adminId,
				"admin@2hands.vn",
				"ACTIVE",
				List.of("ADMIN"),
				List.of("ADMIN_ACCESS")
		));

		var result = useCase.execute(new AdminLoginCommand(
				"admin@2hands.vn",
				"Password123!",
				"127.0.0.1",
				"JUnit",
				"device-1"
		));

		assertThat(result.accessToken()).isEqualTo("access");
		assertThat(result.adminId()).isEqualTo(adminId);
		verify(authLoginGateway).login(any(AdminCredentialLogin.class));
	}

	@Test
	void shouldRejectWhenGatewayDisabled() {
		when(authLoginGateway.isEnabled()).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new AdminLoginCommand(
				"admin@2hands.vn",
				"Password123!",
				null,
				null,
				null
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
	}
}
