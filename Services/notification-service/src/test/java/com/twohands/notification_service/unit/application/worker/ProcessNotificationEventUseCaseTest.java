package com.twohands.notification_service.unit.application.worker;

import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedUseCase;
import com.twohands.notification_service.application.handler.NotificationEventHandler;
import com.twohands.notification_service.application.handler.NotificationEventHandlerRegistry;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedResult;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedResult;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessNotificationEventUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private NotificationEventHandlerRegistry handlerRegistry;

    @Mock
    private MarkNotificationEventCompletedUseCase markNotificationEventCompletedUseCase;

    @Mock
    private MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    @Mock
    private NotificationEventHandler handler;

    private ProcessNotificationEventUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessNotificationEventUseCase(
                notificationEventRepository,
                handlerRegistry,
                markNotificationEventCompletedUseCase,
                markNotificationEventFailedUseCase
        );
    }

    @Test
    void execute_marksCompletedWhenHandlerSucceeds() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, NotificationEventStatus.PROCESSING);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(handlerRegistry.resolve("POST_LIKED")).thenReturn(Optional.of(handler));
        when(handler.handle(event)).thenReturn(NotificationEventHandlerResult.success());
        when(markNotificationEventCompletedUseCase.execute(any(MarkNotificationEventCompletedCommand.class)))
                .thenReturn(new MarkNotificationEventCompletedResult(eventId, Instant.now(), true));

        var result = useCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        verify(markNotificationEventCompletedUseCase).execute(new MarkNotificationEventCompletedCommand(eventId));
        verify(markNotificationEventFailedUseCase, never()).execute(any());
    }

    @Test
    void execute_marksCompletedWhenHandlerReturnsNoOp() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, NotificationEventStatus.PROCESSING);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(handlerRegistry.resolve("POST_LIKED")).thenReturn(Optional.of(handler));
        when(handler.handle(event)).thenReturn(NotificationEventHandlerResult.noOp());
        when(markNotificationEventCompletedUseCase.execute(any(MarkNotificationEventCompletedCommand.class)))
                .thenReturn(new MarkNotificationEventCompletedResult(eventId, Instant.now(), true));

        var result = useCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        verify(markNotificationEventCompletedUseCase).execute(new MarkNotificationEventCompletedCommand(eventId));
    }

    @Test
    void execute_marksFailedWhenHandlerMissing() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, NotificationEventStatus.PROCESSING);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(handlerRegistry.resolve("POST_LIKED")).thenReturn(Optional.empty());
        when(markNotificationEventFailedUseCase.execute(any(MarkNotificationEventFailedCommand.class)))
                .thenReturn(new MarkNotificationEventFailedResult(eventId, 5, 5, true, true));

        var result = useCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        verify(markNotificationEventFailedUseCase).execute(new MarkNotificationEventFailedCommand(
                eventId,
                "Unsupported event type: POST_LIKED",
                NotificationFailurePolicy.PERMANENT
        ));
    }

    @Test
    void execute_marksFailedWhenHandlerReturnsFailure() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, NotificationEventStatus.PROCESSING);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(handlerRegistry.resolve("POST_LIKED")).thenReturn(Optional.of(handler));
        when(handler.handle(event)).thenReturn(NotificationEventHandlerResult.failure(
                "Recipient is required for notification event",
                NotificationFailurePolicy.RETRYABLE
        ));
        when(markNotificationEventFailedUseCase.execute(any(MarkNotificationEventFailedCommand.class)))
                .thenReturn(new MarkNotificationEventFailedResult(eventId, 1, 5, false, true));

        var result = useCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        verify(markNotificationEventFailedUseCase).execute(new MarkNotificationEventFailedCommand(
                eventId,
                "Recipient is required for notification event",
                NotificationFailurePolicy.RETRYABLE
        ));
    }

    @Test
    void execute_skipsAlreadyCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, NotificationEventStatus.COMPLETED);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        var result = useCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.SKIPPED, result.outcome());
        verify(handlerRegistry, never()).resolve(any());
        verify(markNotificationEventCompletedUseCase, never()).execute(any());
        verify(markNotificationEventFailedUseCase, never()).execute(any());
    }

    @Test
    void execute_throwsWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ProcessNotificationEventCommand(eventId))
        );

        assertEquals(ErrorCode.NOTIFICATION_EVENT_NOT_FOUND, ex.getErrorCode());
    }

    private NotificationEvent sampleEvent(UUID eventId, NotificationEventStatus status) {
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
                status,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                status == NotificationEventStatus.COMPLETED ? Instant.now() : null
        );
    }
}
