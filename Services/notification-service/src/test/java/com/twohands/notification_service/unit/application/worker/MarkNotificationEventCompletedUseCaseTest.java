package com.twohands.notification_service.unit.application.worker;

import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationEventCompletedUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    private MarkNotificationEventCompletedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkNotificationEventCompletedUseCase(notificationEventRepository);
    }

    @Test
    void execute_marksProcessingEventCompletedAndClearsLockMetadata() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent processing = sampleEvent(eventId, NotificationEventStatus.PROCESSING, "worker-1", null);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(processing));
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new MarkNotificationEventCompletedCommand(eventId));

        assertTrue(result.updated());
        assertNotNull(result.processedAt());

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        NotificationEvent saved = captor.getValue();
        assertEquals(NotificationEventStatus.COMPLETED, saved.status());
        assertNotNull(saved.processedAt());
        assertNull(saved.lastError());
        assertNull(saved.lockedAt());
        assertNull(saved.lockedBy());
    }

    @Test
    void execute_isIdempotentForAlreadyCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        Instant processedAt = Instant.now();
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
                processedAt
        );

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(completed));

        var result = useCase.execute(new MarkNotificationEventCompletedCommand(eventId));

        assertFalse(result.updated());
        assertEquals(processedAt, result.processedAt());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_canCompletePendingNoOpEvent() {
        UUID eventId = UUID.randomUUID();
        NotificationEvent pending = sampleEvent(eventId, NotificationEventStatus.PENDING, null, null);

        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.of(pending));
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new MarkNotificationEventCompletedCommand(eventId));

        assertTrue(result.updated());
    }

    @Test
    void execute_throwsWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        when(notificationEventRepository.findById(eventId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new MarkNotificationEventCompletedCommand(eventId)));

        assertEquals(ErrorCode.NOTIFICATION_EVENT_NOT_FOUND, ex.getErrorCode());
    }

    private NotificationEvent sampleEvent(
            UUID eventId,
            NotificationEventStatus status,
            String lockedBy,
            Instant processedAt
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
                0,
                5,
                "previous error",
                lockedBy == null ? null : Instant.now(),
                lockedBy,
                Instant.now(),
                processedAt
        );
    }
}
