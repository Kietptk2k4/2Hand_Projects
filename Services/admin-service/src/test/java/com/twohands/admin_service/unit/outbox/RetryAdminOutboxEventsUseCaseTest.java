package com.twohands.admin_service.unit.outbox;

import com.twohands.admin_service.application.outbox.OutboxEventPublisher;
import com.twohands.admin_service.application.outbox.RetryAdminOutboxEventsUseCase;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxTopicResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetryAdminOutboxEventsUseCaseTest {

	private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
	private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
	private final AdminOutboxTopicResolver topicResolver = new AdminOutboxTopicResolver();
	private RetryAdminOutboxEventsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RetryAdminOutboxEventsUseCase(
				outboxEventRepository,
				outboxEventPublisher,
				topicResolver,
				5,
				300,
				50,
				0
		);
	}

	@Test
	void shouldReturnZeroWhenNoRetryCandidates() {
		when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
				.thenReturn(List.of());

		int processed = useCase.execute();

		assertThat(processed).isZero();
		verify(outboxEventPublisher, never()).publish(any());
	}

	@Test
	void shouldRetryFailedEventAndMarkPublished() {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"USER_SUSPENDED",
				aggregateId,
				"{\"user_id\":\"" + aggregateId + "\"}",
				OutboxStatus.PROCESSING,
				2,
				Instant.now().minusSeconds(600),
				null,
				"broker unavailable"
		);
		when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
				.thenReturn(List.of(event));

		int processed = useCase.execute();

		assertThat(processed).isEqualTo(1);
		verify(outboxEventPublisher).publish(event);
		verify(outboxEventRepository).markPublished(eq(eventId), any(Instant.class));
		verify(outboxEventRepository, never()).markFailed(any(), any());
	}

	@Test
	void shouldMarkFailedWhenRetryPublishThrows() {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"USER_BANNED",
				aggregateId,
				"{}",
				OutboxStatus.PROCESSING,
				1,
				Instant.now().minusSeconds(600),
				null,
				"previous error"
		);
		when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
				.thenReturn(List.of(event));
		doThrow(new RuntimeException("broker unavailable")).when(outboxEventPublisher).publish(event);

		int processed = useCase.execute();

		assertThat(processed).isEqualTo(1);
		ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
		verify(outboxEventRepository).markFailed(eq(eventId), errorCaptor.capture());
		assertThat(errorCaptor.getValue()).contains("broker unavailable");
		verify(outboxEventRepository, never()).markPublished(any(), any());
	}

	@Test
	void shouldSkipNonRetryableErrorAndRevertToFailed() {
		UUID eventId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"USER_SUSPENDED",
				UUID.randomUUID(),
				"{}",
				OutboxStatus.PROCESSING,
				1,
				Instant.now().minusSeconds(600),
				null,
				"Unsupported outbox event type for publish: UNKNOWN"
		);
		when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
				.thenReturn(List.of(event));

		int processed = useCase.execute();

		assertThat(processed).isZero();
		verify(outboxEventRepository).revertProcessingToFailed(eventId);
		verify(outboxEventPublisher, never()).publish(any());
	}

	@Test
	void shouldRetryStalePendingEvent() {
		UUID eventId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"PRODUCT_REMOVED",
				UUID.randomUUID(),
				"{}",
				OutboxStatus.PROCESSING,
				0,
				Instant.parse("2026-05-19T08:00:00Z"),
				null,
				null
		);
		when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
				.thenReturn(List.of(event));

		int processed = useCase.execute();

		assertThat(processed).isEqualTo(1);
		verify(outboxEventPublisher).publish(event);
		verify(outboxEventRepository).markPublished(eq(eventId), any(Instant.class));
	}
}
