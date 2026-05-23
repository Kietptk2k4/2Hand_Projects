package com.twohands.admin_service.unit.enforcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.enforcement.UserEnforcementOutboxPayloadBuilder;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementCommand;
import com.twohands.admin_service.application.enforcement.revokeuserenforcement.RevokeUserEnforcementUseCase;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevokeUserEnforcementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);
	private final UserEnforcementLogRepository userEnforcementLogRepository = mock(UserEnforcementLogRepository.class);
	private final AuthUserEnforcementGateway authUserEnforcementGateway = mock(AuthUserEnforcementGateway.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private RevokeUserEnforcementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RevokeUserEnforcementUseCase(
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
	void shouldRevokeActiveRestrictEnforcement() {
		UUID adminId = UUID.randomUUID();
		UUID enforcementId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();
		UserEnforcement active = activeEnforcement(enforcementId, userId, UserEnforcementActionType.RESTRICT, now);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(userEnforcementRepository.findById(enforcementId)).thenReturn(Optional.of(active));
		when(userEnforcementRepository.save(any(UserEnforcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userEnforcementLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(authUserEnforcementGateway.isEnabled()).thenReturn(false);
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(UUID.randomUUID(), "USER_ENFORCEMENT_REVOKED", userId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new RevokeUserEnforcementCommand(enforcementId, "note", null, ""));

		assertThat(result.status()).isEqualTo(UserEnforcementStatus.REVOKED);
		verify(adminAuthorizationService).requirePermission(AdminPermission.USER_ENFORCEMENT_REVOKE);
		verify(authUserEnforcementGateway, never()).revokeEnforcement(any());
	}

	@Test
	void shouldRequestAuthReactivationWhenLastSuspendRevoked() {
		UUID adminId = UUID.randomUUID();
		UUID enforcementId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();
		UserEnforcement active = activeEnforcement(enforcementId, userId, UserEnforcementActionType.SUSPEND, now);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(userEnforcementRepository.findById(enforcementId)).thenReturn(Optional.of(active));
		when(userEnforcementRepository.save(any(UserEnforcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userEnforcementLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.SUSPEND))
				.thenReturn(false);
		when(userEnforcementRepository.existsActiveByUserIdAndActionType(userId, UserEnforcementActionType.BAN))
				.thenReturn(false);
		when(authUserEnforcementGateway.isEnabled()).thenReturn(true);
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(UUID.randomUUID(), "USER_ENFORCEMENT_REVOKED", userId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		useCase.execute(new RevokeUserEnforcementCommand(enforcementId, null, "mistake", "jwt"));

		verify(authUserEnforcementGateway).revokeEnforcement(any(AuthUserEnforcementGateway.AuthRevokeEnforcementRequest.class));
	}

	@Test
	void shouldRejectWhenEnforcementNotActive() {
		UUID adminId = UUID.randomUUID();
		UUID enforcementId = UUID.randomUUID();
		Instant now = Instant.now();
		UserEnforcement revoked = new UserEnforcement(
				enforcementId,
				UUID.randomUUID(),
				UserEnforcementActionType.RESTRICT,
				"SPAM",
				"Spam",
				null,
				UUID.randomUUID(),
				UserEnforcementStatus.REVOKED,
				now,
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(userEnforcementRepository.findById(enforcementId)).thenReturn(Optional.of(revoked));

		assertThatThrownBy(() -> useCase.execute(new RevokeUserEnforcementCommand(enforcementId, null, null, "")))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.ENFORCEMENT_CONFLICT);

		verify(userEnforcementRepository, never()).save(any());
	}

	@Test
	void shouldReturn404WhenEnforcementMissing() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(userEnforcementRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new RevokeUserEnforcementCommand(UUID.randomUUID(), null, null, "")))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private UserEnforcement activeEnforcement(
			UUID enforcementId,
			UUID userId,
			UserEnforcementActionType actionType,
			Instant now
	) {
		return new UserEnforcement(
				enforcementId,
				userId,
				actionType,
				"POLICY",
				"Policy violation",
				null,
				UUID.randomUUID(),
				UserEnforcementStatus.ACTIVE,
				now,
				now
		);
	}
}
