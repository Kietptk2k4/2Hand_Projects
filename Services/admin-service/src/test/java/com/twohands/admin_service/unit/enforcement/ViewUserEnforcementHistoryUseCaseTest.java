package com.twohands.admin_service.unit.enforcement;

import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryQuery;
import com.twohands.admin_service.application.enforcement.viewhistory.ViewUserEnforcementHistoryUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLog;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
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

class ViewUserEnforcementHistoryUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);
	private final UserEnforcementLogRepository userEnforcementLogRepository = mock(UserEnforcementLogRepository.class);
	private final AuthUserLookupGateway authUserLookupGateway = mock(AuthUserLookupGateway.class);

	private ViewUserEnforcementHistoryUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewUserEnforcementHistoryUseCase(
				adminAuthorizationService,
				userEnforcementRepository,
				userEnforcementLogRepository,
				authUserLookupGateway
		);
	}

	@Test
	void shouldReturnPagedEnforcementsWithLogs() {
		UUID userId = UUID.randomUUID();
		UUID enforcementId = UUID.randomUUID();
		Instant now = Instant.now();

		UserEnforcement revoked = new UserEnforcement(
				enforcementId,
				userId,
				UserEnforcementActionType.RESTRICT,
				"SPAM",
				"Spam",
				null,
				UUID.randomUUID(),
				UserEnforcementStatus.REVOKED,
				now.minusSeconds(120),
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserLookupGateway.isEnabled()).thenReturn(false);
		when(userEnforcementRepository.findAllByUserId(userId, new PageRequest(1, 20)))
				.thenReturn(new PagedResult<>(List.of(revoked), 1, 20, 1, 1));
		when(userEnforcementLogRepository.findByEnforcementIdsOrderByCreatedAtDesc(List.of(enforcementId)))
				.thenReturn(List.of(
						new UserEnforcementLog(
								UUID.randomUUID(),
								enforcementId,
								UserEnforcementStatus.ACTIVE,
								UserEnforcementStatus.REVOKED,
								UUID.randomUUID(),
								"Revoked",
								now
						),
						new UserEnforcementLog(
								UUID.randomUUID(),
								enforcementId,
								null,
								UserEnforcementStatus.ACTIVE,
								UUID.randomUUID(),
								"Created",
								now.minusSeconds(60)
						)
				));

		var result = useCase.execute(new ViewUserEnforcementHistoryQuery(userId, 1, 20));

		assertThat(result.totalElements()).isEqualTo(1);
		assertThat(result.enforcements()).hasSize(1);
		assertThat(result.enforcements().get(0).status()).isEqualTo(UserEnforcementStatus.REVOKED);
		assertThat(result.enforcements().get(0).logs()).hasSize(2);
		assertThat(result.enforcements().get(0).logs().get(0).newStatus()).isEqualTo("REVOKED");
		verify(adminAuthorizationService).requirePermission(AdminPermission.USER_ENFORCEMENT_READ);
	}

	@Test
	void shouldRejectInvalidPagination() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.execute(new ViewUserEnforcementHistoryQuery(UUID.randomUUID(), 0, 20)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_PAGINATION);
	}
}
