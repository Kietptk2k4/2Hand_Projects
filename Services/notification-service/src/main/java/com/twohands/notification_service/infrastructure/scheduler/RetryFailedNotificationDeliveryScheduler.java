package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.worker.RetryFailedNotificationDeliveryUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryFailedNotificationDeliveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationDeliveryScheduler.class);

    private final RetryFailedNotificationDeliveryUseCase retryFailedNotificationDeliveryUseCase;
    private final boolean enabled;
    private final int batchSize;

    public RetryFailedNotificationDeliveryScheduler(
            RetryFailedNotificationDeliveryUseCase retryFailedNotificationDeliveryUseCase,
            @Value("${notification.workers.retry-delivery.enabled:false}") boolean enabled,
            @Value("${notification.workers.retry-delivery.batch-size:50}") int batchSize
    ) {
        this.retryFailedNotificationDeliveryUseCase = retryFailedNotificationDeliveryUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${notification.workers.retry-delivery.cron:0 */2 * * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        int processed = retryFailedNotificationDeliveryUseCase.execute(batchSize);
        if (processed > 0) {
            log.info("Retry notification delivery job completed. processedNotifications={}", processed);
        }
    }
}
