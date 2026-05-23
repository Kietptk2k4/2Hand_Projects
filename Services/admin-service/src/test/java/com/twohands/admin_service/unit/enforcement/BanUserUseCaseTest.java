package com.twohands.admin_service.unit.enforcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.enforcement.UserEnforcementOutboxPayloadBuilder;
import com.twohands.admin_service.application.enforcement.banuser.BanUserCommand;
import com.twohands.admin_service.application.enforcement.banuser.BanUserUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BanUserUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);
	private final UserEnforcementLogRepository userEnforcementLogRepository = mock(UserEnforcementLogRepository.class);
	private final AuthUserEnforcementGateway authUserEnforcementGateway = mock(AuthUserEnforcementGateway.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private BanUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new BanUserUseCase(
				adminAuthorizationService,
				userEnforcementRepository,
				userEnforcementLogRepository,
				authUserEnforcementGateway,
				insertAdminOutboxEventUseCase,
				new UserEnforcementOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldBanUserWithAuthIntegrationAndOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID outboxId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(true);
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.BAN))
				.thenReturn(false);
		when(userEnforcementRepository.save(any(UserEnforcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userEnforcementLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(outboxId, "USER_BANNED", userId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new BanUserCommand(
				userId,
				"FRAUD",
				"Confirmed fraud",
				null,
				"bearer-token"
		));

		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.status()).isEqualTo(UserEnforcementStatus.ACTIVE);
		assertThat(result.outboxEventId()).isEqualTo(outboxId);
		verify(adminAuthorizationService).requireAnyPermission(AdminPermission.USER_BAN, AdminPermission.USER_SUSPEND);
		verify(authUserEnforcementGateway).banUser(any(AuthUserEnforcementGateway.AuthBanUserRequest.class));
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
		verify(adminActionAuditLogger).logCritical(
				eq(adminId),
				eq("USER_BAN"),
				any(),
				eq(userId.toString()),
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
	void shouldBanWithoutAuthWhenIntegrationDisabled() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.BAN))
				.thenReturn(false);
		when(userEnforcementRepository.save(any(UserEnforcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userEnforcementLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(UUID.randomUUID(), "USER_BANNED", userId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		useCase.execute(new BanUserCommand(userId, "FRAUD", "Fraud", null, "jwt"));

		verify(authUserEnforcementGateway, never()).banUser(any());
	}

	@Test
	void shouldRejectDuplicateActiveBan() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.BAN))
				.thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(new BanUserCommand(
				userId, "FRAUD", "Fraud", null, "jwt"
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENFORCEMENT_CONFLICT);

		verify(userEnforcementRepository, never()).save(any());
	}

	@Test
	void shouldAuditFailureWhenAuthFails() {
		UUID adminId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(true);
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.BAN))
				.thenReturn(false);
		doThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"))
				.when(authUserEnforcementGateway).banUser(any());

		assertThatThrownBy(() -> useCase.execute(new BanUserCommand(
				userId, "FRAUD", "Fraud", null, "jwt"
		))).isInstanceOf(AppException.class);

		verify(adminActionAuditLogger).logFailure(
				eq(adminId),
				eq("USER_BAN"),
				any(),
				eq(userId.toString()),
				any(),
				any()
		);
	}
}
