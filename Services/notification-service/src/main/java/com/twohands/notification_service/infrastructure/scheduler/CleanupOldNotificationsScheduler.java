package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.read.CleanupOldNotificationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupOldNotificationsScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleanupOldNotificationsScheduler.class);

    private final CleanupOldNotificationsUseCase cleanupOldNotificationsUseCase;
    private final boolean enabled;
    private final int batchSize;

    public CleanupOldNotificationsScheduler(
            CleanupOldNotificationsUseCase cleanupOldNotificationsUseCase,
            @Value("${notification.workers.cleanup-old-notifications.enabled:false}") boolean enabled,
            @Value("${notification.workers.cleanup-old-notifications.batch-size:100}") int batchSize
    ) {
        this.cleanupOldNotificationsUseCase = cleanupOldNotificationsUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${notification.workers.cleanup-old-notifications.cron:0 0 4 * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        var result = cleanupOldNotificationsUseCase.execute(batchSize);
        if (result.softDeletedCount() > 0) {
            log.info(
                    "Cleanup old notifications job completed. softDeletedCount={} batchesProcessed={}",
                    result.softDeletedCount(),
                    result.batchesProcessed()
            );
        }
    }
}
