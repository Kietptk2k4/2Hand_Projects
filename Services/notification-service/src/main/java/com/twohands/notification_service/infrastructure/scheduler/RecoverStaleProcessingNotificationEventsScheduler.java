package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.idempotency.RecoverStaleProcessingNotificationEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecoverStaleProcessingNotificationEventsScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecoverStaleProcessingNotificationEventsScheduler.class);

    private final RecoverStaleProcessingNotificationEventsUseCase recoverStaleProcessingNotificationEventsUseCase;
    private final boolean enabled;
    private final int batchSize;
    private final long processingTimeoutSeconds;

    public RecoverStaleProcessingNotificationEventsScheduler(
            RecoverStaleProcessingNotificationEventsUseCase recoverStaleProcessingNotificationEventsUseCase,
            @Value("${notification.workers.recover-stale-processing.enabled:false}") boolean enabled,
            @Value("${notification.workers.recover-stale-processing.batch-size:50}") int batchSize,
            @Value("${notification.workers.recover-stale-processing.processing-timeout-seconds:300}") long processingTimeoutSeconds
    ) {
        this.recoverStaleProcessingNotificationEventsUseCase = recoverStaleProcessingNotificationEventsUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }

    @Scheduled(cron = "${notification.workers.recover-stale-processing.cron:0 */5 * * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        int recovered = recoverStaleProcessingNotificationEventsUseCase.execute(batchSize, processingTimeoutSeconds);
        if (recovered > 0) {
            log.info("Recover stale processing notification events job completed. recoveredEvents={}", recovered);
        }
    }
}
