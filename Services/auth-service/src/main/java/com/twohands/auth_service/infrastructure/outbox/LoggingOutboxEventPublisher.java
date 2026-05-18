package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

    @Override
    public void publish(OutboxEvent event) {
        log.info(
                "Outbox publish stub invoked. outboxEventId={}, eventType={}, source={}",
                event.id(),
                event.eventType(),
                event.source()
        );
    }
}
