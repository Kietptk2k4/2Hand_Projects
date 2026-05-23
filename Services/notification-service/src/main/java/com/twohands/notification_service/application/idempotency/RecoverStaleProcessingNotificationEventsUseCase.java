package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RecoverStaleProcessingNotificationEventsUseCase {

    public static final String STALE_PROCESSING_ERROR = "STALE_PROCESSING_RECOVERED";

    private final NotificationEventRepository notificationEventRepository;
    private final MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    public RecoverStaleProcessingNotificationEventsUseCase(
            NotificationEventRepository notificationEventRepository,
            MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.markNotificationEventFailedUseCase = markNotificationEventFailedUseCase;
    }

    @Transactional
    public int execute(int batchSize, long processingTimeoutSeconds) {
        Instant lockedBefore = Instant.now().minusSeconds(processingTimeoutSeconds);
        List<com.twohands.notification_service.domain.notificationevent.NotificationEvent> staleEvents =
                notificationEventRepository.findStaleProcessingEvents(lockedBefore, batchSize);

        int recovered = 0;
        for (var event : staleEvents) {
            markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                    event.id(),
                    STALE_PROCESSING_ERROR,
                    NotificationFailurePolicy.RETRYABLE
            ));
            recovered++;
        }
        return recovered;
    }
}
