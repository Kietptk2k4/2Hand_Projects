package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

    private final AuthOutboxTopicResolver topicResolver;

    public LoggingOutboxEventPublisher(AuthOutboxTopicResolver topicResolver) {
        this.topicResolver = topicResolver;
    }

    @Override
    public void publish(OutboxEvent event) {
        String topic = topicResolver.resolve(event.eventType());
        log.info(
                "Outbox publish stub invoked. outboxEventId={}, eventType={}, topic={}, source={}, payloadBytes={}",
                event.id(),
                event.eventType(),
                topic,
                event.source(),
                event.payload() != null ? event.payload().length() : 0
        );
    }
}
