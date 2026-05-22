package com.twohands.commerce_service.unit.application.outbox;

import com.twohands.commerce_service.application.outbox.OutboxEventPublisher;
import com.twohands.commerce_service.application.outbox.RetryCommerceOutboxEventsUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
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

class RetryCommerceOutboxEventsUseCaseTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
    private final RetryCommerceOutboxEventsUseCase useCase =
            new RetryCommerceOutboxEventsUseCase(outboxEventRepository, outboxEventPublisher, 5, 300, 50);

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
                "COMMERCE_ORDER_CREATED",
                "commerce:order:" + aggregateId,
                aggregateId,
                "commerce",
                "{\"order_id\":\"" + aggregateId + "\"}",
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
                "COMMERCE_PAYMENT_PAID",
                "commerce:payment:" + aggregateId,
                aggregateId,
                "payment",
                "{\"payment_id\":\"" + aggregateId + "\"}",
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
    void shouldRetryStalePendingEvent() {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "COMMERCE_SHIPMENT_CREATED",
                "shipment:" + aggregateId + ":created",
                aggregateId,
                "commerce",
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

    @Test
    void shouldClearLastErrorOnSuccessfulRetry() {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "COMMERCE_REVIEW_CREATED",
                "review:" + aggregateId + ":created",
                aggregateId,
                "commerce",
                "{}",
                OutboxStatus.PROCESSING,
                3,
                Instant.now().minusSeconds(900),
                null,
                "stale broker error"
        );
        when(outboxEventRepository.claimRetryCandidates(eq(50), eq(5), any(Instant.class)))
                .thenReturn(List.of(event));

        useCase.execute();

        verify(outboxEventRepository).markPublished(eq(eventId), any(Instant.class));
        verify(outboxEventRepository, never()).markFailed(any(), any());
    }
}
