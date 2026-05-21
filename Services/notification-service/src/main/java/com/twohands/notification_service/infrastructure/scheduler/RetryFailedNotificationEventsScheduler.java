package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.worker.RetryFailedNotificationEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryFailedNotificationEventsScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationEventsScheduler.class);

    private final RetryFailedNotificationEventsUseCase retryFailedNotificationEventsUseCase;
    private final boolean enabled;
    private final int batchSize;

    public RetryFailedNotificationEventsScheduler(
            RetryFailedNotificationEventsUseCase retryFailedNotificationEventsUseCase,
            @Value("${notification.workers.retry-events.enabled:false}") boolean enabled,
            @Value("${notification.workers.retry-events.batch-size:50}") int batchSize
    ) {
        this.retryFailedNotificationEventsUseCase = retryFailedNotificationEventsUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${notification.workers.retry-events.cron:0 */1 * * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        int processed = retryFailedNotificationEventsUseCase.execute(batchSize);
        if (processed > 0) {
            log.info("Retry notification events job completed. processedEvents={}", processed);
        }
    }
}
