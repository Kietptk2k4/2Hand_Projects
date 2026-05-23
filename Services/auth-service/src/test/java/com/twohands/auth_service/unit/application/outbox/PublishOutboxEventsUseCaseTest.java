package com.twohands.auth_service.unit.application.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.application.outbox.PublishOutboxEventsUseCase;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.infrastructure.outbox.AuthOutboxTopicResolver;
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

class PublishOutboxEventsUseCaseTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
    private final AuthOutboxTopicResolver topicResolver = new AuthOutboxTopicResolver();
    private final PublishOutboxEventsUseCase useCase = new PublishOutboxEventsUseCase(
            outboxEventRepository,
            outboxEventPublisher,
            topicResolver,
            5,
            50
    );

    @Test
    void shouldReturnZeroWhenNoPendingEvents() {
        when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of());

        int processed = useCase.execute();

        assertThat(processed).isZero();
        verify(outboxEventPublisher, never()).publish(any());
    }

    @Test
    void shouldPublishAndMarkPublishedOnSuccess() {
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "USER_CREATED",
                "auth-service",
                "{\"user_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"email\":\"u@example.com\"}",
                OutboxStatus.PROCESSING,
                0,
                Instant.parse("2026-05-19T10:00:00Z"),
                null,
                null
        );
        when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of(event));

        int processed = useCase.execute();

        assertThat(processed).isEqualTo(1);
        verify(outboxEventPublisher).publish(event);
        verify(outboxEventRepository).markPublished(eq(eventId), any(Instant.class));
        verify(outboxEventRepository, never()).markFailed(any(), any());
    }

    @Test
    void shouldMarkFailedWhenPublisherThrows() {
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "PASSWORD_CHANGED",
                "auth-service",
                "{\"user_id\":\"550e8400-e29b-41d4-a716-446655440000\"}",
                OutboxStatus.PROCESSING,
                0,
                Instant.now(),
                null,
                null
        );
        when(outboxEventRepository.claimPublishCandidates(50, 5)).thenReturn(List.of(event));
        doThrow(new RuntimeException("broker unavailable")).when(outboxEventPublisher).publish(event);

        int processed = useCase.execute();

        assertThat(processed).isEqualTo(1);
        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxEventRepository).markFailed(eq(eventId), errorCaptor.capture());
        assertThat(errorCaptor.getValue()).contains("broker unavailable");
        verify(outboxEventRepository, never()).markPublished(any(), any());
    }
}
