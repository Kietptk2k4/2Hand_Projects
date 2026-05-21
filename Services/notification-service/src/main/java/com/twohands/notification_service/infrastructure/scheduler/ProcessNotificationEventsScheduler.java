package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.worker.ProcessPendingNotificationEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProcessNotificationEventsScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProcessNotificationEventsScheduler.class);

    private final ProcessPendingNotificationEventsUseCase processPendingNotificationEventsUseCase;
    private final boolean enabled;
    private final int batchSize;

    public ProcessNotificationEventsScheduler(
            ProcessPendingNotificationEventsUseCase processPendingNotificationEventsUseCase,
            @Value("${notification.workers.process-events.enabled:false}") boolean enabled,
            @Value("${notification.workers.process-events.batch-size:50}") int batchSize
    ) {
        this.processPendingNotificationEventsUseCase = processPendingNotificationEventsUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${notification.workers.process-events.cron:0/30 * * * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        int processed = processPendingNotificationEventsUseCase.execute(batchSize);
        if (processed > 0) {
            log.info("Process notification events job completed. processedEvents={}", processed);
        }
    }
}
