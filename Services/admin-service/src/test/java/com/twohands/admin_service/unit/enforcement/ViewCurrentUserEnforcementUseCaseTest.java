package com.twohands.admin_service.unit.enforcement;

import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementQuery;
import com.twohands.admin_service.application.enforcement.viewcurrent.ViewCurrentUserEnforcementUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewCurrentUserEnforcementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);
	private final AuthUserLookupGateway authUserLookupGateway = mock(AuthUserLookupGateway.class);

	private ViewCurrentUserEnforcementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewCurrentUserEnforcementUseCase(
				adminAuthorizationService,
				userEnforcementRepository,
				authUserLookupGateway
		);
	}

	@Test
	void shouldReturnActiveEnforcementsOnly() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();
		UserEnforcement restrict = activeEnforcement(UUID.randomUUID(), userId, UserEnforcementActionType.RESTRICT, now);
		UserEnforcement suspend = activeEnforcement(UUID.randomUUID(), userId, UserEnforcementActionType.SUSPEND, now.minusSeconds(60));

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserLookupGateway.isEnabled()).thenReturn(false);
		when(userEnforcementRepository.findAllActiveByUserId(userId)).thenReturn(List.of(restrict, suspend));

		var result = useCase.execute(new ViewCurrentUserEnforcementQuery(userId));

		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.enforcements()).hasSize(2);
		assertThat(result.enforcements().get(0).actionType()).isEqualTo(UserEnforcementActionType.RESTRICT);
		verify(adminAuthorizationService).requirePermission(AdminPermission.USER_ENFORCEMENT_READ);
	}

	@Test
	void shouldFlagPossiblyExpiredWhenExpiresAtPassed() {
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();
		UserEnforcement expiredSoon = new UserEnforcement(
				UUID.randomUUID(),
				userId,
				UserEnforcementActionType.RESTRICT,
				"SPAM",
				"Spam",
				now.minusSeconds(3600),
				UUID.randomUUID(),
				UserEnforcementStatus.ACTIVE,
				now.minusSeconds(7200),
				now.minusSeconds(7200)
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserLookupGateway.isEnabled()).thenReturn(false);
		when(userEnforcementRepository.findAllActiveByUserId(userId)).thenReturn(List.of(expiredSoon));

		var result = useCase.execute(new ViewCurrentUserEnforcementQuery(userId));

		assertThat(result.enforcements()).singleElement()
				.extracting(item -> item.possiblyExpired())
				.isEqualTo(true);
	}

	@Test
	void shouldValidateUserExistsWhenAuthEnabled() {
		UUID userId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserLookupGateway.isEnabled()).thenReturn(true);
		when(userEnforcementRepository.findAllActiveByUserId(userId)).thenReturn(List.of());

		useCase.execute(new ViewCurrentUserEnforcementQuery(userId));

		verify(authUserLookupGateway).ensureUserExists(userId);
	}

	@Test
	void shouldPropagateUserNotFoundFromAuth() {
		UUID userId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserLookupGateway.isEnabled()).thenReturn(true);
		org.mockito.Mockito.doThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"))
				.when(authUserLookupGateway).ensureUserExists(userId);

		assertThatThrownBy(() -> useCase.execute(new ViewCurrentUserEnforcementQuery(userId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private UserEnforcement activeEnforcement(
			UUID enforcementId,
			UUID userId,
			UserEnforcementActionType actionType,
			Instant createdAt
	) {
		return new UserEnforcement(
				enforcementId,
				userId,
				actionType,
				"POLICY",
				"Policy",
				null,
				UUID.randomUUID(),
				UserEnforcementStatus.ACTIVE,
				createdAt,
				createdAt
		);
	}
}
