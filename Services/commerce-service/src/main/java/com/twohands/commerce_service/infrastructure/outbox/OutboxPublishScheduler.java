package com.twohands.commerce_service.infrastructure.outbox;

import com.twohands.commerce_service.application.outbox.PublishCommerceEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishScheduler.class);

    private final PublishCommerceEventsUseCase publishCommerceEventsUseCase;
    private final boolean enabled;

    public OutboxPublishScheduler(
            PublishCommerceEventsUseCase publishCommerceEventsUseCase,
            @Value("${commerce.outbox.publish.enabled:false}") boolean enabled
    ) {
        this.publishCommerceEventsUseCase = publishCommerceEventsUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${commerce.outbox.publish.cron:0/10 * * * * *}")
    public void runPublishJob() {
        if (!enabled) {
            return;
        }

        int processed = publishCommerceEventsUseCase.execute();
        if (processed > 0) {
            log.info("Outbox publish job completed. processedEvents={}", processed);
        }
    }
}
