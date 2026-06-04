package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.PublishOutboxEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishScheduler.class);

    private final PublishOutboxEventsUseCase publishOutboxEventsUseCase;
    private final boolean enabled;

    public OutboxPublishScheduler(
            PublishOutboxEventsUseCase publishOutboxEventsUseCase,
            @Value("${auth.outbox.publish.enabled:false}") boolean enabled
    ) {
        this.publishOutboxEventsUseCase = publishOutboxEventsUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${auth.outbox.publish.cron:0/1 * * * * *}")
    public void runPublishJob() {
        if (!enabled) {
            return;
        }

        int processed = publishOutboxEventsUseCase.execute();
        if (processed > 0) {
            log.info("Outbox publish job completed. processedEvents={}", processed);
        }
    }
}
