package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.refreshadmintoken.RefreshAdminTokenCommand;
import com.twohands.admin_service.application.auth.refreshadmintoken.RefreshAdminTokenUseCase;
import com.twohands.admin_service.domain.auth.AdminRefreshTokenRequest;
import com.twohands.admin_service.domain.auth.AdminRefreshedAccessToken;
import com.twohands.admin_service.domain.auth.AuthRefreshGateway;
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

class RefreshAdminTokenUseCaseTest {

	private final AuthRefreshGateway authRefreshGateway = mock(AuthRefreshGateway.class);
	private final RefreshAdminTokenUseCase useCase = new RefreshAdminTokenUseCase(authRefreshGateway);

	@Test
	void shouldDelegateRefreshWhenGatewayEnabled() {
		UUID adminId = UUID.randomUUID();
		when(authRefreshGateway.isEnabled()).thenReturn(true);
		when(authRefreshGateway.refresh(any(AdminRefreshTokenRequest.class))).thenReturn(new AdminRefreshedAccessToken(
				"new-access",
				900L,
				adminId,
				"admin@2hands.vn",
				"ACTIVE",
				List.of("ADMIN"),
				List.of("ADMIN_ACCESS")
		));

		var result = useCase.execute(new RefreshAdminTokenCommand("refresh-token-value", "127.0.0.1"));

		assertThat(result.accessToken()).isEqualTo("new-access");
		assertThat(result.adminId()).isEqualTo(adminId);
		verify(authRefreshGateway).refresh(any(AdminRefreshTokenRequest.class));
	}

	@Test
	void shouldRejectWhenGatewayDisabled() {
		when(authRefreshGateway.isEnabled()).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new RefreshAdminTokenCommand("refresh-token", null)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
	}
}
