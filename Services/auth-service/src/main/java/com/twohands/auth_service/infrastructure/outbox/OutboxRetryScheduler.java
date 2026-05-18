package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.RetryFailedOutboxEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRetryScheduler.class);

    private final RetryFailedOutboxEventsUseCase retryFailedOutboxEventsUseCase;
    private final boolean enabled;

    public OutboxRetryScheduler(
            RetryFailedOutboxEventsUseCase retryFailedOutboxEventsUseCase,
            @Value("${auth.outbox.retry.enabled:false}") boolean enabled
    ) {
        this.retryFailedOutboxEventsUseCase = retryFailedOutboxEventsUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${auth.outbox.retry.cron:0 */1 * * * *}")
    public void runRetryJob() {
        if (!enabled) {
            return;
        }

        int processed = retryFailedOutboxEventsUseCase.execute();
        if (processed > 0) {
            log.info("Outbox retry job completed. processedEvents={}", processed);
        }
    }
}
