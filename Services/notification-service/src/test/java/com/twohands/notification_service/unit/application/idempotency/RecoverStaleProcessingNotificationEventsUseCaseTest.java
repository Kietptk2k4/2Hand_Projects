package com.twohands.notification_service.unit.application.idempotency;

import com.twohands.notification_service.application.idempotency.RecoverStaleProcessingNotificationEventsUseCase;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedResult;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoverStaleProcessingNotificationEventsUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    private RecoverStaleProcessingNotificationEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RecoverStaleProcessingNotificationEventsUseCase(
                notificationEventRepository,
                markNotificationEventFailedUseCase
        );
    }

    @Test
    void execute_delegatesStaleEventsToMarkFailedUseCase() {
        NotificationEvent stale = staleEvent();
        when(notificationEventRepository.findStaleProcessingEvents(any(Instant.class), eq(10)))
                .thenReturn(List.of(stale));
        when(markNotificationEventFailedUseCase.execute(any(MarkNotificationEventFailedCommand.class)))
                .thenReturn(new MarkNotificationEventFailedResult(stale.id(), 1, 5, false, true));

        int recovered = useCase.execute(10, 300);

        assertEquals(1, recovered);
        verify(markNotificationEventFailedUseCase).execute(new MarkNotificationEventFailedCommand(
                stale.id(),
                RecoverStaleProcessingNotificationEventsUseCase.STALE_PROCESSING_ERROR,
                NotificationFailurePolicy.RETRYABLE
        ));
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
