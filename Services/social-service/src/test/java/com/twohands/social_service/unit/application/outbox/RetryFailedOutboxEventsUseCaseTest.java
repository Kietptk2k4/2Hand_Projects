package com.twohands.social_service.unit.application.outbox;

import com.twohands.social_service.application.outbox.OutboxEventPublisher;
import com.twohands.social_service.application.outbox.RetryFailedOutboxEventsUseCase;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.outbox.OutboxStatus;
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

class RetryFailedOutboxEventsUseCaseTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
    private final RetryFailedOutboxEventsUseCase useCase =
            new RetryFailedOutboxEventsUseCase(outboxEventRepository, outboxEventPublisher, 5, 300, 50);

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
        OutboxEvent event = new OutboxEvent(
                eventId,
                "POST_LIKED",
                "507f1f77bcf86cd799439011",
                "{\"post_id\":\"507f1f77bcf86cd799439011\"}",
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
        OutboxEvent event = new OutboxEvent(
                eventId,
                "USER_FOLLOWED",
                "550e8400-e29b-41d4-a716-446655440000",
                "{\"follower_id\":\"a\",\"followee_id\":\"b\"}",
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
}
