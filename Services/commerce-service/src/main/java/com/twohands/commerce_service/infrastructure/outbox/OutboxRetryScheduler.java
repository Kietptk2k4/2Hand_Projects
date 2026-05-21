package com.twohands.commerce_service.infrastructure.outbox;

import com.twohands.commerce_service.application.outbox.RetryCommerceOutboxEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRetryScheduler.class);

    private final RetryCommerceOutboxEventsUseCase retryCommerceOutboxEventsUseCase;
    private final boolean enabled;

    public OutboxRetryScheduler(
            RetryCommerceOutboxEventsUseCase retryCommerceOutboxEventsUseCase,
            @Value("${commerce.outbox.retry.enabled:false}") boolean enabled
    ) {
        this.retryCommerceOutboxEventsUseCase = retryCommerceOutboxEventsUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${commerce.outbox.retry.cron:0 */1 * * * *}")
    public void runRetryJob() {
        if (!enabled) {
            return;
        }

        int processed = retryCommerceOutboxEventsUseCase.execute();
        if (processed > 0) {
            log.info("Outbox retry job completed. processedEvents={}", processed);
        }
    }
}
