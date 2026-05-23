package com.twohands.notification_service.unit.application.worker;

import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventResult;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessPendingNotificationEventsUseCase;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPendingNotificationEventsUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    private ProcessPendingNotificationEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPendingNotificationEventsUseCase(
                notificationEventRepository,
                processNotificationEventUseCase
        );
    }

    @Test
    void execute_returnsZeroWhenBatchSizeInvalid() {
        assertEquals(0, useCase.execute(0));
        verify(notificationEventRepository, never()).claimProcessableEvents(anyInt(), anyString());
    }

    @Test
    void execute_returnsZeroWhenNoEventsClaimed() {
        when(notificationEventRepository.claimProcessableEvents(10, "notification-processor"))
                .thenReturn(List.of());

        assertEquals(0, useCase.execute(10));
        verify(processNotificationEventUseCase, never()).execute(any());
    }

    @Test
    void execute_processesClaimedEvents() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        NotificationEvent first = sampleEvent(firstId);
        NotificationEvent second = sampleEvent(secondId);

        when(notificationEventRepository.claimProcessableEvents(2, "notification-processor"))
                .thenReturn(List.of(first, second));
        when(processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(firstId)))
                .thenReturn(new ProcessNotificationEventResult(firstId, ProcessNotificationEventOutcome.COMPLETED));
        when(processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(secondId)))
                .thenReturn(new ProcessNotificationEventResult(secondId, ProcessNotificationEventOutcome.FAILED));

        assertEquals(2, useCase.execute(2));
        verify(processNotificationEventUseCase).execute(new ProcessNotificationEventCommand(firstId));
        verify(processNotificationEventUseCase).execute(new ProcessNotificationEventCommand(secondId));
    }

    private NotificationEvent sampleEvent(UUID eventId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                UUID.randomUUID(),
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "notification-processor",
                Instant.now(),
                null
        );
    }
}
