package com.twohands.admin_service.unit.outbox;

import com.twohands.admin_service.application.outbox.OutboxEventPublisher;
import com.twohands.admin_service.application.outbox.PublishAdminEventsUseCase;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxTopicResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishAdminEventsUseCaseTest {

	@Mock
	private OutboxEventRepository outboxEventRepository;

	@Mock
	private OutboxEventPublisher outboxEventPublisher;

	private final AdminOutboxTopicResolver topicResolver = new AdminOutboxTopicResolver();

	private PublishAdminEventsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublishAdminEventsUseCase(
				outboxEventRepository,
				outboxEventPublisher,
				topicResolver,
				5,
				50
		);
	}

	@Test
	void execute_returnsZeroWhenNoCandidates() {
		when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of());

		assertEquals(0, useCase.execute());
		verify(outboxEventPublisher, never()).publish(any());
	}

	@Test
	void execute_publishesAndMarksPublished() {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"USER_SUSPENDED",
				aggregateId,
				"{\"user_id\":\"" + aggregateId + "\"}",
				OutboxStatus.PROCESSING,
				0,
				Instant.now(),
				null,
				null
		);
		when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of(event));

		assertEquals(1, useCase.execute());

		verify(outboxEventPublisher).publish(event);
		verify(outboxEventRepository).markPublished(eq(eventId), any(Instant.class));
		verify(outboxEventRepository, never()).markFailed(any(), any());
	}

	@Test
	void execute_marksFailedWhenPublisherThrows() {
		UUID eventId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"PRODUCT_REMOVED",
				UUID.randomUUID(),
				"{\"product_id\":\"p1\"}",
				OutboxStatus.PROCESSING,
				1,
				Instant.now(),
				null,
				null
		);
		when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of(event));
		doThrow(new RuntimeException("broker unavailable")).when(outboxEventPublisher).publish(event);

		assertEquals(1, useCase.execute());

		ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
		verify(outboxEventRepository).markFailed(eq(eventId), errorCaptor.capture());
		assertTrue(errorCaptor.getValue().contains("broker unavailable"));
		verify(outboxEventRepository, never()).markPublished(any(), any());
	}
}
