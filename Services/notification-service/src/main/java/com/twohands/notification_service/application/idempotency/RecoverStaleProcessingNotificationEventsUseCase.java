package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.domain.idempotency.NotificationErrorSanitizer;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RecoverStaleProcessingNotificationEventsUseCase {

    public static final String STALE_PROCESSING_ERROR = "STALE_PROCESSING_RECOVERED";

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationErrorSanitizer errorSanitizer;

    public RecoverStaleProcessingNotificationEventsUseCase(
            NotificationEventRepository notificationEventRepository,
            NotificationErrorSanitizer errorSanitizer
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.errorSanitizer = errorSanitizer;
    }

    @Transactional
    public int execute(int batchSize, long processingTimeoutSeconds) {
        Instant lockedBefore = Instant.now().minusSeconds(processingTimeoutSeconds);
        List<NotificationEvent> staleEvents = notificationEventRepository.findStaleProcessingEvents(
                lockedBefore,
                batchSize
        );

        int recovered = 0;
        for (NotificationEvent event : staleEvents) {
            NotificationEvent recoveredEvent = new NotificationEvent(
                    event.id(),
                    event.sourceEventId(),
                    event.eventKey(),
                    event.eventType(),
                    event.sourceService(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.actorId(),
                    event.recipientUserId(),
                    event.payload(),
                    NotificationEventStatus.FAILED,
                    Math.min(event.retryCount() + 1, event.maxRetryCount()),
                    event.maxRetryCount(),
                    errorSanitizer.sanitize(STALE_PROCESSING_ERROR),
                    null,
                    null,
                    event.createdAt(),
                    null
            );
            notificationEventRepository.save(recoveredEvent);
            recovered++;
        }
        return recovered;
    }
}
