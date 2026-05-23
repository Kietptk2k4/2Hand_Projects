package com.twohands.notification_service.unit.application.idempotency;

import com.twohands.notification_service.application.idempotency.BoundedNotificationErrorSanitizer;
import com.twohands.notification_service.application.idempotency.RecoverStaleProcessingNotificationEventsUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoverStaleProcessingNotificationEventsUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    private RecoverStaleProcessingNotificationEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RecoverStaleProcessingNotificationEventsUseCase(
                notificationEventRepository,
                new BoundedNotificationErrorSanitizer()
        );
    }

    @Test
    void execute_marksStaleProcessingEventsAsFailedRetryable() {
        NotificationEvent stale = staleEvent();
        when(notificationEventRepository.findStaleProcessingEvents(any(Instant.class), eq(10)))
                .thenReturn(List.of(stale));

        int recovered = useCase.execute(10, 300);

        assertEquals(1, recovered);
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        NotificationEvent saved = captor.getValue();
        assertEquals(NotificationEventStatus.FAILED, saved.status());
        assertEquals(1, saved.retryCount());
        assertEquals(RecoverStaleProcessingNotificationEventsUseCase.STALE_PROCESSING_ERROR, saved.lastError());
        assertNull(saved.lockedAt());
        assertNull(saved.lockedBy());
    }

    private NotificationEvent staleEvent() {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now().minusSeconds(600),
                "worker-1",
                Instant.now().minusSeconds(900),
                null
        );
    }
}
