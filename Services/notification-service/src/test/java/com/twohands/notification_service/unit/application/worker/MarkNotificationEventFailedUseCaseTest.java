package com.twohands.notification_service.unit.application.worker;

import com.twohands.notification_service.application.idempotency.BoundedNotificationErrorSanitizer;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationEventFailedUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    private MarkNotificationEventFailedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkNotificationEventFailedUseCase(
                notificationEventRepository,
                new BoundedNotificationErrorSanitizer()
        );
    }

    @Test
    void execute_marksRetryableFailureAndIncrementsRetryCount() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent processing = sampleEvent(eventId, NotificationEventStatus.PROCESSING, 1, "worker-1");

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(processing));
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "Handler missing for event type",
                NotificationFailurePolicy.RETRYABLE
        ));

        assertTrue(result.updated());
        assertEquals(2, result.retryCount());
        assertFalse(result.permanentFailure());

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        NotificationEvent saved = captor.getValue();
        assertEquals(NotificationEventStatus.FAILED, saved.status());
        assertEquals(2, saved.retryCount());
        assertEquals("Handler missing for event type", saved.lastError());
        assertNull(saved.lockedAt());
        assertNull(saved.lockedBy());
        assertNull(saved.processedAt());
    }

    @Test
    void execute_marksPermanentFailureAtMaxRetryCount() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent processing = sampleEvent(eventId, NotificationEventStatus.PROCESSING, 2, "worker-1");

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(processing));
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "UNSUPPORTED_EVENT_TYPE",
                NotificationFailurePolicy.PERMANENT
        ));

        assertTrue(result.updated());
        assertEquals(5, result.retryCount());
        assertTrue(result.permanentFailure());
    }

    @Test
    void execute_sanitizesSensitiveErrorMessage() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent processing = sampleEvent(eventId, NotificationEventStatus.PROCESSING, 0, "worker-1");

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(processing));
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "provider failure token=abc123",
                NotificationFailurePolicy.RETRYABLE
        ));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        assertTrue(captor.getValue().lastError().contains("***REDACTED***"));
        assertFalse(captor.getValue().lastError().contains("abc123"));
    }

    @Test
    void execute_skipsAlreadyCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent completed = new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.COMPLETED,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(completed));

        var result = useCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "late failure",
                NotificationFailurePolicy.RETRYABLE
        ));

        assertFalse(result.updated());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_throwsWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new MarkNotificationEventFailedCommand(eventId, "missing", NotificationFailurePolicy.RETRYABLE)
        ));

        assertEquals(ErrorCode.NOTIFICATION_EVENT_NOT_FOUND, ex.getErrorCode());
    }

    private NotificationEvent sampleEvent(
            UUID eventId,
            NotificationEventStatus status,
            int retryCount,
            String lockedBy
    ) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                status,
                retryCount,
                5,
                null,
                Instant.now(),
                lockedBy,
                Instant.now(),
                null
        );
    }
}
