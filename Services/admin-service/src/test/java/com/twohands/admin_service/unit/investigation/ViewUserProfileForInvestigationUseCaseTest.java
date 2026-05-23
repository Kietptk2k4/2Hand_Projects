package com.twohands.admin_service.unit.investigation;

import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationQuery;
import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.integration.AuthUserInvestigationGateway;
import com.twohands.admin_service.domain.integration.InvestigationUserProfile;
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

class ViewUserProfileForInvestigationUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final AuthUserInvestigationGateway authUserInvestigationGateway = mock(AuthUserInvestigationGateway.class);
	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);

	private ViewUserProfileForInvestigationUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewUserProfileForInvestigationUseCase(
				adminAuthorizationService,
				authUserInvestigationGateway,
				userEnforcementRepository
		);
	}

	@Test
	void shouldComposeProfileWithCurrentEnforcements() {
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserInvestigationGateway.isEnabled()).thenReturn(true);
		when(authUserInvestigationGateway.fetchInvestigationProfile(userId, "token"))
				.thenReturn(new InvestigationUserProfile(
						userId,
						"user@example.com",
						"ACTIVE",
						true,
						false,
						now,
						now.minusSeconds(3600),
						"Display",
						null,
						null,
						null,
						false
				));
		when(userEnforcementRepository.findAllActiveByUserId(userId))
				.thenReturn(List.of(new UserEnforcement(
						UUID.randomUUID(),
						userId,
						UserEnforcementActionType.RESTRICT,
						"SPAM",
						"Spam",
						null,
						UUID.randomUUID(),
						UserEnforcementStatus.ACTIVE,
						now,
						now
				)));

		var result = useCase.execute(new ViewUserProfileForInvestigationQuery(userId, "token"));

		assertThat(result.email()).isEqualTo("user@example.com");
		assertThat(result.currentEnforcements()).hasSize(1);
		assertThat(result.currentEnforcements().get(0).actionType()).isEqualTo(UserEnforcementActionType.RESTRICT);
		verify(adminAuthorizationService).requirePermission(AdminPermission.USER_INVESTIGATION_READ);
	}

	@Test
	void shouldRejectWhenAuthIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserInvestigationGateway.isEnabled()).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new ViewUserProfileForInvestigationQuery(UUID.randomUUID(), "token")))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SERVICE_UNAVAILABLE);
	}
}
