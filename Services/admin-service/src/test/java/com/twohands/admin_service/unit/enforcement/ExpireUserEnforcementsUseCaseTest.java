package com.twohands.admin_service.unit.enforcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.enforcement.UserEnforcementOutboxPayloadBuilder;
import com.twohands.admin_service.application.enforcement.expire.ExpireUserEnforcementsUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpireUserEnforcementsUseCaseTest {

	private final UserEnforcementRepository userEnforcementRepository = mock(UserEnforcementRepository.class);
	private final UserEnforcementLogRepository userEnforcementLogRepository = mock(UserEnforcementLogRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);

	private ExpireUserEnforcementsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ExpireUserEnforcementsUseCase(
				userEnforcementRepository,
				userEnforcementLogRepository,
				insertAdminOutboxEventUseCase,
				new UserEnforcementOutboxPayloadBuilder(new ObjectMapper()),
				50
		);
	}

	@Test
	void shouldExpireActiveEnforcementWithPastExpiresAt() {
		UUID userId = UUID.randomUUID();
		UUID enforcementId = UUID.randomUUID();
		Instant now = Instant.now();
		Instant expiresAt = now.minusSeconds(60);

		UserEnforcement active = new UserEnforcement(
				enforcementId,
				userId,
				UserEnforcementActionType.SUSPEND,
				"POLICY",
				"Temporary suspend",
				expiresAt,
				UUID.randomUUID(),
				UserEnforcementStatus.ACTIVE,
				now.minusSeconds(3600),
				now.minusSeconds(3600)
		);

		when(userEnforcementRepository.claimActiveExpiredEnforcements(any(), any(Integer.class)))
				.thenReturn(List.of(active));
		when(userEnforcementRepository.save(any(UserEnforcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(
						UUID.randomUUID(),
						"USER_ENFORCEMENT_EXPIRED",
						userId,
						"{}",
						OutboxStatus.PENDING,
						0,
						now,
						null,
						null
				));

		var result = useCase.execute();

		assertThat(result.expiredCount()).isEqualTo(1);
		verify(userEnforcementLogRepository).save(any());
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldReturnZeroWhenNoCandidates() {
		when(userEnforcementRepository.claimActiveExpiredEnforcements(any(), any(Integer.class)))
				.thenReturn(List.of());

		var result = useCase.execute();

		assertThat(result.expiredCount()).isZero();
		verify(userEnforcementLogRepository, never()).save(any());
		verify(insertAdminOutboxEventUseCase, never()).execute(any());
	}

	@Test
	void shouldSkipNonActiveClaimedRow() {
		Instant now = Instant.now();
		UserEnforcement revoked = new UserEnforcement(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UserEnforcementActionType.RESTRICT,
				"SPAM",
				"Already revoked",
				now.minusSeconds(30),
				UUID.randomUUID(),
				UserEnforcementStatus.REVOKED,
				now.minusSeconds(120),
				now
		);

		when(userEnforcementRepository.claimActiveExpiredEnforcements(any(), any(Integer.class)))
				.thenReturn(List.of(revoked));

		var result = useCase.execute();

		assertThat(result.expiredCount()).isZero();
		verify(userEnforcementRepository, never()).save(any());
	}
}
