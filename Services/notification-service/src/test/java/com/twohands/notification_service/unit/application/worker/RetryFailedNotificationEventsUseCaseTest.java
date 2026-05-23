package com.twohands.notification_service.unit.application.worker;

import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventResult;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.application.worker.RetryFailedNotificationEventsUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryFailedNotificationEventsUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    private RetryFailedNotificationEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RetryFailedNotificationEventsUseCase(
                notificationEventRepository,
                processNotificationEventUseCase,
                30,
                3600
        );
    }

    @Test
    void execute_returnsZeroWhenBatchSizeInvalid() {
        assertEquals(0, useCase.execute(0));
        verify(notificationEventRepository, never()).claimRetryableFailedEvents(
                anyInt(),
                anyString(),
                any(Instant.class),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void execute_returnsZeroWhenNoEligibleEventsClaimed() {
        when(notificationEventRepository.claimRetryableFailedEvents(
                eq(10),
                eq("notification-retry-processor"),
                any(Instant.class),
                eq(30),
                eq(3600)
        )).thenReturn(List.of());

        assertEquals(0, useCase.execute(10));
        verify(processNotificationEventUseCase, never()).execute(any());
    }

    @Test
    void execute_reprocessesClaimedFailedEvents() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent claimed = sampleFailedEvent(eventId);

        when(notificationEventRepository.claimRetryableFailedEvents(
                eq(1),
                eq("notification-retry-processor"),
                any(Instant.class),
                eq(30),
                eq(3600)
        )).thenReturn(List.of(claimed));
        when(processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId)))
                .thenReturn(new ProcessNotificationEventResult(eventId, ProcessNotificationEventOutcome.COMPLETED));

        assertEquals(1, useCase.execute(1));
        verify(processNotificationEventUseCase).execute(new ProcessNotificationEventCommand(eventId));
    }

    private NotificationEvent sampleFailedEvent(UUID eventId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "{}",
                NotificationEventStatus.PROCESSING,
                1,
                5,
                "Recipient missing",
                Instant.now().minusSeconds(60),
                "notification-retry-processor",
                Instant.now(),
                null
        );
    }
}
