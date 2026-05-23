package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RetryFailedNotificationEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationEventsUseCase.class);
    private static final String WORKER_ID = "notification-retry-processor";

    private final NotificationEventRepository notificationEventRepository;
    private final ProcessNotificationEventUseCase processNotificationEventUseCase;
    private final int baseBackoffSeconds;
    private final int maxBackoffSeconds;

    public RetryFailedNotificationEventsUseCase(
            NotificationEventRepository notificationEventRepository,
            ProcessNotificationEventUseCase processNotificationEventUseCase,
            @Value("${notification.workers.retry-events.base-backoff-seconds:30}") int baseBackoffSeconds,
            @Value("${notification.workers.retry-events.max-backoff-seconds:3600}") int maxBackoffSeconds
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.processNotificationEventUseCase = processNotificationEventUseCase;
        this.baseBackoffSeconds = baseBackoffSeconds;
        this.maxBackoffSeconds = maxBackoffSeconds;
    }

    public int execute(int batchSize) {
        if (batchSize <= 0) {
            return 0;
        }

        Instant now = Instant.now();
        List<NotificationEvent> claimedEvents = notificationEventRepository.claimRetryableFailedEvents(
                batchSize,
                WORKER_ID,
                now,
                baseBackoffSeconds,
                maxBackoffSeconds
        );
        if (claimedEvents.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (NotificationEvent event : claimedEvents) {
            ProcessNotificationEventResult result = processNotificationEventUseCase.execute(
                    new ProcessNotificationEventCommand(event.id())
            );
            if (result.outcome() != ProcessNotificationEventOutcome.SKIPPED) {
                processed++;
            }
            log.debug(
                    "Retried notification event. eventId={}, outcome={}",
                    event.id(),
                    result.outcome()
            );
        }
        return processed;
    }
}
